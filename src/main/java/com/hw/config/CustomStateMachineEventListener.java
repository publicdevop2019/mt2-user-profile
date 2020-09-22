package com.hw.config;

import com.hw.aggregate.order.PaymentService;
import com.hw.aggregate.order.ProductService;
import com.hw.aggregate.order.model.BizOrderEvent;
import com.hw.aggregate.order.model.BizOrderStatus;
import com.hw.aggregate.task.AppBizTaskApplicationService;
import com.hw.aggregate.task.command.AppUpdateBizTaskCommand;
import com.hw.aggregate.task.model.BizTaskStatus;
import com.hw.aggregate.task.representation.AppBizTaskRep;
import com.hw.shared.rest.CreatedEntityRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.hw.config.AppConstant.TX_TASK;

@Slf4j
@Component
public class CustomStateMachineEventListener
        extends StateMachineListenerAdapter<BizOrderStatus, BizOrderEvent> {
    public static final String ERROR_CLASS = "ERROR_CLASS";
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Autowired
    private AppBizTaskApplicationService appBizTaskApplicationService;

    @Override
    public void stateMachineError(StateMachine<BizOrderStatus, BizOrderEvent> stateMachine, Exception exception) {
        log.error("start of stateMachineError, rollback transaction", exception);
        //set error class so it can be thrown later, thrown ex here will still result 200 response
        stateMachine.getExtendedState().getVariables().put(ERROR_CLASS, exception);
        CreatedEntityRep createdTask = stateMachine.getExtendedState().get(TX_TASK, CreatedEntityRep.class);
        if (createdTask != null) {
            AppBizTaskRep appBizTaskRep = appBizTaskApplicationService.readById(createdTask.getId());
            String[] split = exception.getClass().getName().split("\\.");
            rollback(createdTask, appBizTaskRep, split[split.length - 1]);
        } else {
            log.info("error happened in non-transactional context, no rollback will be triggered");
        }
    }

    private void rollback(CreatedEntityRep entityRep, AppBizTaskRep transactionalTask, String exceptionName) {
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
            return;
        }
        log.info("rollback transaction async call complete");
        try {
            AppUpdateBizTaskCommand appUpdateBizTaskCommand = new AppUpdateBizTaskCommand();
            appUpdateBizTaskCommand.setTaskStatus(BizTaskStatus.ROLLBACK);
            appUpdateBizTaskCommand.setRollbackReason(exceptionName);
            appBizTaskApplicationService.replaceById(entityRep.getId(), appUpdateBizTaskCommand, UUID.randomUUID().toString());
        } catch (Exception ex) {
            log.info("error during task status update, task remain in started status", ex);
        }
    }
}
