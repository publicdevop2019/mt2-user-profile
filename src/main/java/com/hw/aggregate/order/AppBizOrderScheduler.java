package com.hw.aggregate.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.exception.BizOrderSchedulerProductRecycleException;
import com.hw.aggregate.order.exception.BizOrderSchedulerTaskRollbackException;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderStatus;
import com.hw.aggregate.task.BizTaskRepository;
import com.hw.aggregate.task.model.BizTask;
import com.hw.aggregate.task.model.BizTaskStatus;
import com.hw.config.TransactionIdGenerator;
import com.hw.shared.DeepCopyException;
import com.hw.shared.sql.PatchCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.hw.shared.AppConstant.PATCH_OP_TYPE_DIFF;
import static com.hw.shared.AppConstant.PATCH_OP_TYPE_SUM;

/**
 * make sure state machine, guard and action is referring to same entity
 * so version will always be consistent, otherwise state machine might trigger wrong operation
 * or guard & action will get incorrectly committed
 */
@Service
@Slf4j
@EnableScheduling
public class AppBizOrderScheduler {

    @Value("${order.expireAfter}")
    private Long expireAfter;

    @Value("${task.expireAfter}")
    private Long taskExpireAfter;

    @Autowired
    private BizOrderRepository bizOrderRepository;

    @Autowired
    private BizTaskRepository taskRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper om;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserBizOrderApplicationService userBizOrderApplicationService;


    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.release}")
    public void releaseExpiredOrder() {
        new TransactionTemplate(transactionManager)
                .execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        String transactionId = TransactionIdGenerator.getTxId();
                        Date from = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - expireAfter * 60 * 1000));
                        List<BizOrder> expiredOrderList = bizOrderRepository.findExpiredNotPaidReserved(from);
                        List<PatchCommand> details = new ArrayList<>();
                        expiredOrderList.forEach(expiredOrder -> {
                            List<PatchCommand> var1 = expiredOrder.getReserveOrderPatchCommands();
                            details.addAll(var1);
                        });
                        List<PatchCommand> deepCopy = getDeepCopy(details);
                        deepCopy.forEach(e -> {
                            if (e.getOp().equalsIgnoreCase(PATCH_OP_TYPE_SUM)) {
                                e.setOp(PATCH_OP_TYPE_DIFF);
                            } else {
                                e.setOp(PATCH_OP_TYPE_SUM);
                            }
                        });
                        try {
                            if (!details.isEmpty()) {
                                log.info("expired order(s) found {}", expiredOrderList.stream().map(BizOrder::getId).collect(Collectors.toList()).toString());
                                productService.updateProductStorage(details, transactionId);
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
                    userBizOrderApplicationService.submitOrder(order.getId(), order.getCreatedBy());
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
        List<BizTask> tasks = taskRepository.findExpiredStartedTasks(from);
        if (!tasks.isEmpty()) {
            log.info("expired & started task found {}", tasks.stream().map(BizTask::getId).collect(Collectors.toList()));
            tasks.forEach(task -> {
                try {
                    new TransactionTemplate(transactionManager)
                            .execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus status) {
                                    // read task again make sure it's still valid & apply opt lock
                                    Optional<BizTask> byIdOptLock = taskRepository.findByIdOptLock(task.getId());
                                    if (byIdOptLock.isPresent()
                                            && byIdOptLock.get().getCreatedAt().compareTo(from) < 0
                                            && byIdOptLock.get().getTaskStatus().equals(BizTaskStatus.STARTED)
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

    private void rollback(BizTask transactionalTask) {
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
        transactionalTask.setTaskStatus(BizTaskStatus.ROLLBACK);
        try {
            taskRepository.saveAndFlush(transactionalTask);
        } catch (Exception ex) {
            log.info("error during task status update, task remain in started status", ex);
            throw new BizOrderSchedulerTaskRollbackException();
        }
    }

    private List<PatchCommand> getDeepCopy(List<PatchCommand> patchCommands) {
        List<PatchCommand> deepCopy;
        try {
            deepCopy = om.readValue(om.writeValueAsString(patchCommands), new TypeReference<List<PatchCommand>>() {
            });
        } catch (IOException e) {
            log.error("error during deep copy", e);
            throw new DeepCopyException();
        }
        return deepCopy;
    }
}
