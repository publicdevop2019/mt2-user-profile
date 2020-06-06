package com.hw.config;

import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.PaymentService;
import com.hw.aggregate.order.ProductService;
import com.hw.aggregate.order.model.OrderEvent;
import com.hw.aggregate.order.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CustomStateMachineEventListener
        extends StateMachineListenerAdapter<OrderState, OrderEvent> {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartApplicationService cartApplicationService;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Override
    public void stateMachineError(StateMachine<OrderState, OrderEvent> stateMachine, Exception exception) {
        log.error("start of stateMachineError, rollback transaction", exception);
        String transactionId = stateMachine.getExtendedState().get("transactionId", String.class);
        CompletableFuture.runAsync(() ->
                cartApplicationService.rollbackTransaction(transactionId), customExecutor
        );
        CompletableFuture.runAsync(() ->
                paymentService.rollbackTransaction(transactionId), customExecutor
        );
        CompletableFuture.runAsync(() ->
                productService.rollbackTransaction(transactionId), customExecutor
        );
    }
}
