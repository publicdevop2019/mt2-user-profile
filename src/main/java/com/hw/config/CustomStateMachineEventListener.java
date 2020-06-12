package com.hw.config;

import com.hw.aggregate.order.PaymentService;
import com.hw.aggregate.order.ProductService;
import com.hw.aggregate.order.TransactionalTaskRepository;
import com.hw.aggregate.order.model.BizOrderEvent;
import com.hw.aggregate.order.model.BizOrderStatus;
import com.hw.aggregate.order.model.TaskStatus;
import com.hw.aggregate.order.model.TransactionalTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.hw.aggregate.order.model.AppConstant.TX_TASK;

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
    private TransactionalTaskRepository taskRepository;

    @Override
    public void stateMachineError(StateMachine<BizOrderStatus, BizOrderEvent> stateMachine, Exception exception) {
        log.error("start of stateMachineError, rollback transaction", exception);
        //set error class so it can be thrown later, thrown ex here will still result 200 response
        stateMachine.getExtendedState().getVariables().put(ERROR_CLASS, exception);
        TransactionalTask transactionalTask = stateMachine.getExtendedState().get(TX_TASK, TransactionalTask.class);
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
        } catch (ExecutionException e) {
            log.error("error during rollback transaction async call", e);
        }
        transactionalTask.setTaskStatus(TaskStatus.ROLLBACK);
        try {
            taskRepository.saveAndFlush(transactionalTask);
        } catch (Exception ex) {
            log.error("error during task status update, task remain in started status", ex);
        }
    }
}
