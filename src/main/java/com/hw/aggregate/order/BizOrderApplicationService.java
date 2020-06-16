package com.hw.aggregate.order;

import com.hw.aggregate.order.command.CreateBizOrderCommand;
import com.hw.aggregate.order.command.PlaceBizOrderAgainCommand;
import com.hw.aggregate.order.exception.BizOrderSchedulerProductRecycleException;
import com.hw.aggregate.order.exception.BizOrderSchedulerTaskRollbackException;
import com.hw.aggregate.order.model.*;
import com.hw.aggregate.order.representation.*;
import com.hw.config.CustomStateMachineBuilder;
import com.hw.config.ProfileExistAndOwnerOnly;
import com.hw.config.TransactionIdGenerator;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.IdGenerator;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.hw.aggregate.order.model.AppConstant.BIZ_ORDER;
import static com.hw.aggregate.order.model.AppConstant.UPDATE_ADDRESS_CMD;
import static com.hw.config.CustomStateMachineEventListener.ERROR_CLASS;

/**
 * make sure state machine, guard and action is referring to same entity
 * so version will always be consistent, otherwise state machine might trigger wrong operation
 * or guard & action will get incorrectly committed
 */
@Service
@Slf4j
@EnableScheduling
public class BizOrderApplicationService {

    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Value("${order.expireAfter}")
    private Long expireAfter;

    @Value("${task.expireAfter}")
    private Long taskExpireAfter;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    private BizOrderRepository bizOrderRepository;

    @Autowired
    private TransactionalTaskRepository taskRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomStateMachineBuilder customStateMachineBuilder;

    @Autowired
    private PaymentService paymentService;

    @Transactional(readOnly = true)
    public BizOrderSummaryAdminRepresentation getAllOrdersForAdmin() {
        log.info("start of getAllOrdersForAdmin");
        return new BizOrderSummaryAdminRepresentation(bizOrderRepository.findAll());
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public BizOrderSummaryCustomerRepresentation getAllOrders(String userId, Long profileId) {
        log.info("start of getAllOrders");
        return new BizOrderSummaryCustomerRepresentation(bizOrderRepository.findByProfileId(profileId));
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public BizOrderCustomerRepresentation getOrderForCustomer(String userId, Long profileId, Long orderId) {
        log.info("start of getOrderForCustomer");
        return new BizOrderCustomerRepresentation(BizOrder.get(profileId, orderId, bizOrderRepository));
    }

    @ProfileExistAndOwnerOnly
    public BizOrderPaymentLinkRepresentation createNew(String userId, Long profileId, Long orderId, CreateBizOrderCommand command) {
        log.debug("start of createNew {}", orderId);
        BizOrder customerOrder = BizOrder.create(orderId, profileId, command.getProductList(), command.getAddress(), command.getPaymentType(), command.getPaymentAmt());
        StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
        stateMachine.sendEvent(BizOrderEvent.PREPARE_NEW_ORDER);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        stateMachine.sendEvent(BizOrderEvent.NEW_ORDER);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        return new BizOrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public BizOrderConfirmStatusRepresentation confirmPayment(String userId, Long profileId, Long orderId) {
        log.debug("start of confirmPayment {}", orderId);
        BizOrder customerOrder = BizOrder.getWOptLock(profileId, orderId, bizOrderRepository);
        StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
        stateMachine.sendEvent(BizOrderEvent.CONFIRM_PAYMENT);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        return new BizOrderConfirmStatusRepresentation(customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    public void confirmOrder(String userId, Long profileId, Long orderId) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.debug("start of confirmOrder {}", orderId);
                BizOrder customerOrder = BizOrder.getWOptLock(profileId, orderId, bizOrderRepository);
                StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
                stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
                stateMachine.sendEvent(BizOrderEvent.PREPARE_CONFIRM_ORDER);
                if (stateMachine.hasStateMachineError()) {
                    throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
                }
                stateMachine.sendEvent(BizOrderEvent.CONFIRM_ORDER);
                if (stateMachine.hasStateMachineError()) {
                    throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
                }
            }
        });
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public BizOrderPaymentLinkRepresentation reserveAgain(String userId, Long profileId, Long orderId, PlaceBizOrderAgainCommand command) {
        log.info("reserve order {} again", orderId);
        BizOrder customerOrder = BizOrder.getWOptLock(profileId, orderId, bizOrderRepository);
        StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(UPDATE_ADDRESS_CMD, command);
        stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
        stateMachine.sendEvent(BizOrderEvent.PREPARE_RESERVE);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        stateMachine.sendEvent(BizOrderEvent.RESERVE);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        return new BizOrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String userId, Long profileId, Long orderId) {
        BizOrder customerOrder = BizOrder.getWOptLock(profileId, orderId, bizOrderRepository);
        bizOrderRepository.delete(customerOrder);
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.release}")
    public void releaseExpiredOrder() {
        new TransactionTemplate(transactionManager)
                .execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        String transactionId = TransactionIdGenerator.getTxId();
                        Date from = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - expireAfter * 60 * 1000));
                        List<BizOrder> expiredOrderList = bizOrderRepository.findExpiredNotPaidReserved(from);
                        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
                        expiredOrderList.forEach(expiredOrder -> {
                            Map<String, Integer> orderProductMap = expiredOrder.getProductSummary();
                            orderProductMap.forEach((key, value) -> stringIntegerHashMap.merge(key, value, Integer::sum));
                        });
                        try {
                            if (!stringIntegerHashMap.keySet().isEmpty()) {
                                log.info("expired order(s) found {}", expiredOrderList.stream().map(BizOrder::getId).collect(Collectors.toList()).toString());
                                productService.increaseOrderStorage(stringIntegerHashMap, transactionId);
                                /** update order state*/
                                expiredOrderList.forEach(e -> {
                                    e.setOrderState(BizOrderStatus.NOT_PAID_RECYCLED);
                                });
                                log.info("expired order(s) released");
                                bizOrderRepository.saveAll(expiredOrderList);
                                bizOrderRepository.flush();
                            }
                        } catch (Exception ex) {
                            log.error("error during release storage, revoke last operation", ex);
                            CompletableFuture.runAsync(() ->
                                    productService.rollbackTransaction(transactionId), customExecutor
                            );
                            throw new BizOrderSchedulerProductRecycleException();
                        }
                    }
                });
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.resubmit}")
    public void resubmitOrder() {
        List<BizOrder> paidReserved = bizOrderRepository.findPaidReserved();
        if (!paidReserved.isEmpty()) {
            log.info("paid reserved order(s) found {}", paidReserved.stream().map(BizOrder::getId).collect(Collectors.toList()));
            // submit one order for now
            paidReserved.forEach(order -> {
                try {
                    confirmOrder(null, order.getProfileId(), order.getId());
                    log.info("resubmit order {} success", order.getId());
                } catch (Exception e) {
                    log.error("resubmit order {} failed", order.getId(), e);
                }
            });
        }
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.taskRollback}")
    public void rollbackTask() {
        Date from = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - taskExpireAfter * 60 * 1000));
        List<TransactionalTask> tasks = taskRepository.findExpiredStartedTasks(from);
        if (!tasks.isEmpty()) {
            log.info("expired & started task found {}", tasks.stream().map(TransactionalTask::getId).collect(Collectors.toList()));
            tasks.forEach(task -> {
                try {
                    new TransactionTemplate(transactionManager)
                            .execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus status) {
                                    // read task again make sure it's still valid & apply opt lock
                                    Optional<TransactionalTask> byIdOptLock = taskRepository.findByIdOptLock(task.getId());
                                    if (byIdOptLock.isPresent()
                                            && byIdOptLock.get().getCreatedAt().compareTo(from) < 0
                                            && byIdOptLock.get().getTaskStatus().equals(TaskStatus.STARTED)
                                    ) {
                                        rollback(task);
                                    }

                                }
                            });
                    log.info("rollback task {} success", task.getId());
                } catch (Exception e) {
                    log.error("rollback task {} failed", task.getId(), e);
                }
            });
        }
    }

    public String getOrderId() {
        return String.valueOf(idGenerator.getId());
    }

    private void rollback(TransactionalTask transactionalTask) {
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() ->
                paymentService.rollbackTransaction(transactionalTask.getTransactionId()), customExecutor
        );
        CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() ->
                productService.rollbackTransaction(transactionalTask.getTransactionId()), customExecutor
        );
        CompletableFuture<Void> voidCompletableFuture2 = CompletableFuture.allOf(voidCompletableFuture, voidCompletableFuture1);
        try {
            voidCompletableFuture2.get();
        } catch (InterruptedException e) {
            log.warn("thread was interrupted", e);
            Thread.currentThread().interrupt();
            return;
        } catch (ExecutionException e) {
            log.error("error during rollback transaction async call", e);
            throw new BizOrderSchedulerTaskRollbackException();
        }
        log.info("rollback transaction async call complete");
        transactionalTask.setTaskStatus(TaskStatus.ROLLBACK);
        try {
            taskRepository.saveAndFlush(transactionalTask);
        } catch (Exception ex) {
            log.info("error during task status update, task remain in started status", ex);
            throw new BizOrderSchedulerTaskRollbackException();
        }
    }
}
