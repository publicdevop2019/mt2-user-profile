package com.hw.config;

import com.hw.aggregate.order.PaymentService;
import com.hw.aggregate.order.ProductService;
import com.hw.aggregate.order.model.OrderEvent;
import com.hw.aggregate.order.model.OrderStatus;
import com.hw.aggregate.order.model.TransactionalTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.hw.aggregate.order.model.AppConstant.TX_TASK;

@Slf4j
@Component
public class CustomStateMachineEventListener
        extends StateMachineListenerAdapter<OrderStatus, OrderEvent> {
    public static final String ERROR_CLASS = "ERROR_CLASS";
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Override
    public void stateMachineError(StateMachine<OrderStatus, OrderEvent> stateMachine, Exception exception) {
        log.error("start of stateMachineError, rollback transaction", exception);
        //set error class so it can be thrown later, thrown ex here will still result 200 response
        stateMachine.getExtendedState().getVariables().put(ERROR_CLASS, exception);
        TransactionalTask transactionalTask = stateMachine.getExtendedState().get(TX_TASK, TransactionalTask.class);
        CompletableFuture.runAsync(() ->
                paymentService.rollbackTransaction(transactionalTask.getTransactionId()), customExecutor
        );
        CompletableFuture.runAsync(() ->
                productService.rollbackTransaction(transactionalTask.getTransactionId()), customExecutor
        );
    }
}
