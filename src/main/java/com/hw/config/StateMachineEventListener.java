package com.hw.config;

import com.hw.aggregate.order.model.OrderEvent;
import com.hw.aggregate.order.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
@Slf4j
public class StateMachineEventListener
        extends StateMachineListenerAdapter<OrderState, OrderEvent> {

    @Override
    public void stateChanged(State<OrderState, OrderEvent> from, State<OrderState, OrderEvent> to) {
        log.info("stateChanged");
    }

    @Override
    public void stateEntered(State<OrderState, OrderEvent> state) {
        log.info("stateEntered");
    }

    @Override
    public void stateExited(State<OrderState, OrderEvent> state) {
        log.info("stateExited");
    }

    @Override
    public void transition(Transition<OrderState, OrderEvent> transition) {
        log.info("transition");
    }

    @Override
    public void transitionStarted(Transition<OrderState, OrderEvent> transition) {
        log.info("transitionStarted");
    }

    @Override
    public void transitionEnded(Transition<OrderState, OrderEvent> transition) {
        log.info("transitionEnded");
    }

    @Override
    public void stateMachineStarted(StateMachine<OrderState, OrderEvent> stateMachine) {
        log.info("stateMachineStarted");
    }

    @Override
    public void stateMachineStopped(StateMachine<OrderState, OrderEvent> stateMachine) {
        log.info("stateMachineStopped");
    }

    @Override
    public void eventNotAccepted(Message<OrderEvent> event) {
        log.info("eventNotAccepted");
    }

    @Override
    public void extendedStateChanged(Object key, Object value) {
        log.info("extendedStateChanged");
    }

    @Override
    public void stateMachineError(StateMachine<OrderState, OrderEvent> stateMachine, Exception exception) {
        log.info("stateMachineError");
    }

    @Override
    public void stateContext(StateContext<OrderState, OrderEvent> stateContext) {
        log.info("stateContext");
    }
}
