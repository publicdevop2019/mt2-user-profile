package com.hw.config;

import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.*;
import com.hw.aggregate.order.exception.*;
import com.hw.aggregate.order.model.*;
import com.hw.shared.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.hw.aggregate.order.model.AppConstant.ORDER_DETAIL;
import static com.hw.aggregate.order.model.AppConstant.TX_TASK;

/**
 * each guard is an unit of work, roll back when failure happen
 */
@Configuration
@Slf4j
public class CustomStateMachineBuilder {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MessengerService messengerService;

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private CartApplicationService cartApplicationService;

    @Autowired
    private CustomerOrderRepository orderRepository;

    @Autowired
    private TransactionalTaskRepository taskRepository;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomStateMachineEventListener customStateMachineEventListener;

    @Autowired
    private IdGenerator idGenerator;

    public StateMachine<OrderStatus, OrderEvent> buildMachine(OrderStatus initialState) {
        StateMachineBuilder.Builder<OrderStatus, OrderEvent> builder = StateMachineBuilder.builder();
        try {
            builder.configureConfiguration()
                    .withConfiguration()
                    .autoStartup(true)
                    .listener(customStateMachineEventListener)
            ;
            builder.configureStates()
                    .withStates()
                    .initial(initialState)
                    .states(EnumSet.allOf(OrderStatus.class));
            builder.configureTransitions()
                    .withInternal()
                    .source(OrderStatus.DRAFT)
                    .event(OrderEvent.PREPARE)
                    .action(prepareTaskFor(OrderEvent.NEW_ORDER))
                    .and()
                    .withExternal()
                    .source(OrderStatus.DRAFT).target(OrderStatus.NOT_PAID_RESERVED)
                    .event(OrderEvent.NEW_ORDER)
                    .guard(createNewOrderTask())
                    .and()
                    .withExternal()
                    .source(OrderStatus.NOT_PAID_RESERVED).target(OrderStatus.PAID_RESERVED)
                    .event(OrderEvent.CONFIRM_PAYMENT)
                    .guard(updatePaymentStatus())
                    .action(autoConfirm())
                    .and()
                    .withExternal()
                    .source(OrderStatus.NOT_PAID_RECYCLED).target(OrderStatus.PAID_RECYCLED)
                    .event(OrderEvent.CONFIRM_PAYMENT)
                    .guard(updatePaymentStatus())
                    .and()
                    .withInternal()
                    .source(OrderStatus.PAID_RECYCLED)
                    .event(OrderEvent.PREPARE)
                    .action(prepareTaskFor(OrderEvent.RESERVE))
                    .and()
                    .withExternal()
                    .source(OrderStatus.PAID_RECYCLED).target(OrderStatus.PAID_RESERVED)
                    .event(OrderEvent.RESERVE)
                    .guard(reserveOrderTask())
                    .action(autoConfirm())
                    .and()
                    .withInternal()
                    .source(OrderStatus.NOT_PAID_RECYCLED)
                    .event(OrderEvent.PREPARE)
                    .action(prepareTaskFor(OrderEvent.RESERVE))
                    .and()
                    .withExternal()
                    .source(OrderStatus.NOT_PAID_RECYCLED).target(OrderStatus.NOT_PAID_RESERVED)
                    .event(OrderEvent.RESERVE)
                    .guard(reserveOrderTask())
                    .and()
//                    done by scheduler, state machine is not used there
//                    .withExternal()
//                    .source(OrderState.NOT_PAID_RESERVED).target(OrderState.NOT_PAID_RECYCLED)
//                    .event(OrderEvent.RECYCLE_ORDER_STORAGE)
//                    .guard(updateCustomerOrder())
//                    .and()
                    .withInternal()
                    .source(OrderStatus.PAID_RESERVED)
                    .event(OrderEvent.PREPARE)
                    .action(prepareTaskFor(OrderEvent.CONFIRM_ORDER))
                    .and()
                    .withExternal()
                    .source(OrderStatus.PAID_RESERVED).target(OrderStatus.CONFIRMED)
                    .event(OrderEvent.CONFIRM_ORDER)
                    .guard(confirmOrderTask())
                    .action(sendNotification())
            ;
        } catch (Exception e) {
            log.error("error during creating state machine");
            throw new StateMachineCreationException();
        }
        return builder.build();
    }

    private Action<OrderStatus, OrderEvent> prepareTaskFor(OrderEvent event) {
        return context -> {
            log.info("start of save task to database");
            try {
                String txId = TransactionIdGenerator.getTxId();
                CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
                TransactionalTask transactionalTask = new TransactionalTask(idGenerator.getId(), event, TaskStatus.STARTED, txId, customerOrder.getId());
                context.getExtendedState().getVariables().put(TX_TASK, transactionalTask);
                taskRepository.saveAndFlush(transactionalTask);
            } catch (Exception ex) {
                log.error("error during data persist", ex);
                context.getStateMachine().setStateMachineError(new TaskPersistenceException());
            }
        };
    }

    private Action<OrderStatus, OrderEvent> autoConfirm() {
        return context -> {
            log.info("start of autoConfirm");
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            orderApplicationService.confirmOrder(customerOrder.getCreatedBy(), customerOrder.getProfileId(), customerOrder.getId());
        };
    }

    private Guard<OrderStatus, OrderEvent> createNewOrderTask() {
        return context -> {
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            TransactionalTask transactionalTask = context.getExtendedState().get(TX_TASK, TransactionalTask.class);
            log.info("start of prepareNewOrder of {}", customerOrder.getId());
            // validate order product info
            CompletableFuture<Void> validateResultFuture = CompletableFuture.runAsync(() ->
                    productService.validateProductInfo(customerOrder.getReadOnlyProductList()), customExecutor
            );

            // generate payment QR link
            CompletableFuture<String> paymentQRLinkFuture = CompletableFuture.supplyAsync(() ->
                    paymentService.generatePaymentLink(customerOrder.getId().toString(), transactionalTask.getTransactionId()), customExecutor
            );

            // decrease order storage
            CompletableFuture<Void> decreaseOrderStorageFuture = CompletableFuture.runAsync(() ->
                    productService.decreaseOrderStorage(customerOrder.getProductSummary(), transactionalTask.getTransactionId()), customExecutor
            );
            CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(validateResultFuture, paymentQRLinkFuture, decreaseOrderStorageFuture);
            try {
                allDoneFuture.get();
                customerOrder.setPaymentLink(paymentQRLinkFuture.get());
            } catch (ExecutionException ex) {
                log.error("error during prepare order async call", ex);
                if (decreaseOrderStorageFuture.isCompletedExceptionally())
                    context.getStateMachine().setStateMachineError(new OrderStorageDecreaseException());
                if (paymentQRLinkFuture.isCompletedExceptionally())
                    context.getStateMachine().setStateMachineError(new PaymentQRLinkGenerationException());
                if (validateResultFuture.isCompletedExceptionally())
                    context.getStateMachine().setStateMachineError(new ProductInfoValidationException());
                return false;
            } catch (InterruptedException e) {
                log.warn("thread was interrupted", e);
                context.getStateMachine().setStateMachineError(e);
                Thread.currentThread().interrupt();
                return false;
            }
            // manually set order state so it can be update to database
            customerOrder.setOrderState(context.getTarget().getId());

            customerOrder.setTransactionHistory(new HashMap<>());

            customerOrder.getTransactionHistory().put(context.getEvent(), transactionalTask.getTransactionId());
            transactionalTask.setTaskStatus(TaskStatus.COMPLETED);
            // start local transaction, manually rollback since no ex will be thrown
            // not set @Transactional at service level also prevents long running transaction
            // in future if order separate from profile then no need for transaction
            Boolean execute = new TransactionTemplate(transactionManager)
                    .execute(transactionStatus -> {
                        //clear user cart
                        try {
                            cartApplicationService.clearCartItem(customerOrder.getProfileId());
                        } catch (Exception ex) {
                            log.error("error during clear cart", ex);
                            context.getStateMachine().setStateMachineError(new CartClearException());
                            return false;
                        }
                        // save reserved order
                        try {
                            orderRepository.saveAndFlush(customerOrder);
                        } catch (Exception ex) {
                            log.error("error during data persist", ex);
                            context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                            transactionManager.rollback(transactionStatus);
                            return false;
                        }
                        // save task
                        try {
                            taskRepository.saveAndFlush(transactionalTask);
                        } catch (Exception ex) {
                            log.error("error during data persist", ex);
                            context.getStateMachine().setStateMachineError(new TaskPersistenceException());
                            transactionManager.rollback(transactionStatus);
                            return false;
                        }
                        return true;
                    });
            return Boolean.TRUE.equals(execute);
        };
    }

    private Guard<OrderStatus, OrderEvent> reserveOrderTask() {
        return context -> {
            log.info("start of decreaseOrderStorage");
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            TransactionalTask transactionalTask = context.getExtendedState().get(TX_TASK, TransactionalTask.class);
            try {
                productService.decreaseOrderStorage(customerOrder.getProductSummary(), transactionalTask.getTransactionId());
            } catch (Exception ex) {
                log.error("error during decrease order storage");
                context.getStateMachine().setStateMachineError(new OrderStorageDecreaseException());
                return false;
            }
            customerOrder.setOrderState(context.getTarget().getId());

            customerOrder.getTransactionHistory().put(context.getEvent(), transactionalTask.getTransactionId());
            transactionalTask.setTaskStatus(TaskStatus.COMPLETED);

            Boolean execute = new TransactionTemplate(transactionManager)
                    .execute(transactionStatus -> {
                        try {
                            orderRepository.saveAndFlush(customerOrder);
                        } catch (Exception ex) {
                            log.error("error during data persist", ex);
                            context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                            return false;
                        }
                        // save task
                        try {
                            taskRepository.saveAndFlush(transactionalTask);
                        } catch (Exception ex) {
                            log.error("error during data persist", ex);
                            context.getStateMachine().setStateMachineError(new TaskPersistenceException());
                            transactionManager.rollback(transactionStatus);
                            return false;
                        }
                        return true;
                    });
            return Boolean.TRUE.equals(execute);
        };
    }

    private Action<OrderStatus, OrderEvent> sendNotification() {
        return context -> {
            log.info("start of sendEmailNotification");
            messengerService.notifyBusinessOwner(new HashMap<>());
        };
    }

    private Guard<OrderStatus, OrderEvent> confirmOrderTask() {
        return context -> {
            log.info("start of decreaseActualStorage");
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            TransactionalTask transactionalTask = context.getExtendedState().get(TX_TASK, TransactionalTask.class);

            try {
                productService.decreaseActualStorage(customerOrder.getProductSummary(), transactionalTask.getTransactionId());
            } catch (Exception ex) {
                log.error("error during decreaseActualStorage");
                context.getStateMachine().setStateMachineError(new ActualStorageDecreaseException());
                return false;
            }
            customerOrder.setOrderState(context.getTarget().getId());

            customerOrder.getTransactionHistory().put(context.getEvent(), transactionalTask.getTransactionId());
            transactionalTask.setTaskStatus(TaskStatus.COMPLETED);
            Boolean execute = new TransactionTemplate(transactionManager)
                    .execute(transactionStatus -> {
                        try {
                            orderRepository.saveAndFlush(customerOrder);
                        } catch (Exception ex) {
                            log.error("error during data persist", ex);
                            context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                            return false;
                        }
                        // save task
                        try {
                            taskRepository.saveAndFlush(transactionalTask);
                        } catch (Exception ex) {
                            log.error("error during data persist", ex);
                            context.getStateMachine().setStateMachineError(new TaskPersistenceException());
                            transactionManager.rollback(transactionStatus);
                            return false;
                        }
                        return true;
                    });
            return Boolean.TRUE.equals(execute);
        };
    }

    private Guard<OrderStatus, OrderEvent> updatePaymentStatus() {
        return context -> {
            log.info("start of updatePaymentStatus");
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            Boolean paymentStatus = paymentService.confirmPaymentStatus(customerOrder.getId().toString());
            log.info("result {}", paymentStatus.toString());
            customerOrder.setPaid(paymentStatus);
            if (Boolean.TRUE.equals(paymentStatus)) {
                customerOrder.setOrderState(context.getTarget().getId());
            }
            try {
                orderRepository.saveAndFlush(customerOrder);
            } catch (Exception ex) {
                log.error("error during data persist", ex);
                context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                return false;
            }
            return Boolean.TRUE.equals(paymentStatus);
        };
    }


}
