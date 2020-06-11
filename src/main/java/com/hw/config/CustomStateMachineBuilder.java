package com.hw.config;

import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.*;
import com.hw.aggregate.order.exception.*;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.OrderEvent;
import com.hw.aggregate.order.model.OrderState;
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

import static com.hw.shared.AppConstant.ORDER_DETAIL;
import static com.hw.shared.AppConstant.TX_ID;

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
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomStateMachineEventListener customStateMachineEventListener;

    public StateMachine<OrderState, OrderEvent> buildMachine(OrderState initialState) {
        StateMachineBuilder.Builder<OrderState, OrderEvent> builder = StateMachineBuilder.builder();
        try {
            builder.configureConfiguration()
                    .withConfiguration()
                    .autoStartup(true)
                    .listener(customStateMachineEventListener)
            ;
            builder.configureStates()
                    .withStates()
                    .initial(initialState)
                    .states(EnumSet.allOf(OrderState.class));
            builder.configureTransitions()
                    .withExternal()
                    .source(OrderState.DRAFT).target(OrderState.NOT_PAID_RESERVED)
                    .event(OrderEvent.NEW_ORDER)
                    .guard(prepareNewOrder())
                    .and()
                    .withInternal()
                    .source(OrderState.DRAFT)
                    .event(OrderEvent.PERSIST)
                    .action(saveDraft())
                    .and()
                    .withExternal()
                    .source(OrderState.NOT_PAID_RESERVED).target(OrderState.PAID_RESERVED)
                    .event(OrderEvent.CONFIRM_PAYMENT)
                    .guard(updatePaymentStatus())
                    .action(autoConfirm())
                    .and()
                    .withExternal()
                    .source(OrderState.NOT_PAID_RECYCLED).target(OrderState.PAID_RECYCLED)
                    .event(OrderEvent.CONFIRM_PAYMENT)
                    .guard(updatePaymentStatus())
                    .and()
                    .withExternal()
                    .source(OrderState.PAID_RECYCLED).target(OrderState.PAID_RESERVED)
                    .event(OrderEvent.RESERVE)
                    .guard(reserveOrder())
                    .action(autoConfirm())
                    .and()
                    .withExternal()
                    .source(OrderState.NOT_PAID_RECYCLED).target(OrderState.NOT_PAID_RESERVED)
                    .event(OrderEvent.RESERVE)
                    .guard(reserveOrder())
                    .and()
//                    done by scheduler, state machine is not used there
//                    .withExternal()
//                    .source(OrderState.NOT_PAID_RESERVED).target(OrderState.NOT_PAID_RECYCLED)
//                    .event(OrderEvent.RECYCLE_ORDER_STORAGE)
//                    .guard(updateCustomerOrder())
//                    .and()
                    .withExternal()
                    .source(OrderState.PAID_RESERVED).target(OrderState.CONFIRMED)
                    .event(OrderEvent.CONFIRM_ORDER)
                    .guard(confirmOrder())
                    .action(sendNotification())
            ;
        } catch (Exception e) {
            log.error("error during creating state machine");
            throw new StateMachineCreationException();
        }
        return builder.build();
    }

    private Action<OrderState, OrderEvent> saveDraft() {
        return context -> {
            log.info("start of persist draft order");
            try {
                CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
                HashMap<OrderState, String> history = new HashMap<>();
                history.put(context.getSource().getId(), customerOrder.getCurrentTransactionId());
                customerOrder.setTransactionHistory(history);
                customerOrder.setCurrentTransactionId(null);
                customerOrderRepository.saveAndFlush(customerOrder);
            } catch (Exception ex) {
                log.error("error during data persist", ex);
                context.getStateMachine().setStateMachineError(new OrderPersistenceException());
            }
        };
    }

    private Action<OrderState, OrderEvent> autoConfirm() {
        return context -> {
            log.info("start of autoConfirm");
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            orderApplicationService.confirmOrder(customerOrder.getCreatedBy(), customerOrder.getProfileId(), customerOrder.getId());
        };
    }
    private Guard<OrderState, OrderEvent> prepareNewOrder() {
        return context -> {
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            String storedTxId = customerOrder.getNextTransactionId();
            customerOrder.setCurrentTransactionId(storedTxId);
            customerOrder.setNextTransactionId(TransactionIdGenerator.getId());
            context.getExtendedState().getVariables().put(TX_ID, customerOrder.getCurrentTransactionId());
            log.info("start of prepareNewOrder of {}, tx id {}", customerOrder.getId(), customerOrder.getCurrentTransactionId());
            // validate order product info
            CompletableFuture<Void> validateResultFuture = CompletableFuture.runAsync(() ->
                    productService.validateProductInfo(customerOrder.getReadOnlyProductList()), customExecutor
            );

            // generate payment QR link
            CompletableFuture<String> paymentQRLinkFuture = CompletableFuture.supplyAsync(() ->
                    paymentService.generatePaymentLink(customerOrder.getId().toString()), customExecutor
            );

            // decrease order storage
            CompletableFuture<Void> decreaseOrderStorageFuture = CompletableFuture.runAsync(() ->
                    productService.decreaseOrderStorage(customerOrder.getProductSummary(), customerOrder.getCurrentTransactionId()), customExecutor
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
            customerOrder.getTransactionHistory().put(context.getTarget().getId(), customerOrder.getCurrentTransactionId());
            customerOrder.setCurrentTransactionId(null);
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
                            customerOrderRepository.saveAndFlush(customerOrder);
                        } catch (Exception ex) {
                            log.error("error during data persist", ex);
                            context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                            transactionManager.rollback(transactionStatus);
                            return false;
                        }
                        return true;
                    });
            return Boolean.TRUE.equals(execute);
        };
    }

    private Guard<OrderState, OrderEvent> reserveOrder() {
        return context -> {
            log.info("start of decreaseOrderStorage");
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            String storedTxId = customerOrder.getNextTransactionId();
            customerOrder.setCurrentTransactionId(storedTxId);
            customerOrder.setNextTransactionId(TransactionIdGenerator.getId());
            try {
                productService.decreaseOrderStorage(customerOrder.getProductSummary(), customerOrder.getCurrentTransactionId());
            } catch (Exception ex) {
                log.error("error during decrease order storage");
                context.getStateMachine().setStateMachineError(new OrderStorageDecreaseException());
                return false;
            }
            customerOrder.setOrderState(context.getTarget().getId());
            customerOrder.getTransactionHistory().put(context.getTarget().getId(), customerOrder.getCurrentTransactionId());
            customerOrder.setCurrentTransactionId(null);
            try {
                customerOrderRepository.saveAndFlush(customerOrder);
            } catch (Exception ex) {
                log.error("error during data persist", ex);
                context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                return false;
            }
            return true;
        };
    }

    private Action<OrderState, OrderEvent> sendNotification() {
        return context -> {
            log.info("start of sendEmailNotification");
            messengerService.notifyBusinessOwner(new HashMap<>());
        };
    }

    private Guard<OrderState, OrderEvent> confirmOrder() {
        return context -> {
            log.info("start of decreaseActualStorage");
            CustomerOrder customerOrder = context.getExtendedState().get(ORDER_DETAIL, CustomerOrder.class);
            String storedTxId = customerOrder.getNextTransactionId();
            customerOrder.setCurrentTransactionId(storedTxId);
            customerOrder.setNextTransactionId(TransactionIdGenerator.getId());
            try {
                productService.decreaseActualStorage(customerOrder.getProductSummary(), customerOrder.getCurrentTransactionId());
            } catch (Exception ex) {
                log.error("error during decreaseActualStorage");
                context.getStateMachine().setStateMachineError(new ActualStorageDecreaseException());
                return false;
            }
            customerOrder.setOrderState(context.getTarget().getId());
            customerOrder.getTransactionHistory().put(context.getTarget().getId(), customerOrder.getCurrentTransactionId());
            customerOrder.setCurrentTransactionId(null);
            try {
                customerOrderRepository.saveAndFlush(customerOrder);
            } catch (Exception ex) {
                log.error("error during data persist", ex);
                context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                return false;
            }
            return true;
        };
    }

    private Guard<OrderState, OrderEvent> updatePaymentStatus() {
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
                customerOrderRepository.saveAndFlush(customerOrder);
            } catch (Exception ex) {
                log.error("error during data persist", ex);
                context.getStateMachine().setStateMachineError(new OrderPersistenceException());
                return false;
            }
            return Boolean.TRUE.equals(paymentStatus);
        };
    }


}
