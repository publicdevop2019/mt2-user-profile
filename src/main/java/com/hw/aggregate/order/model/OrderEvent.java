package com.hw.aggregate.order.model;

import com.hw.aggregate.order.exception.StateChangeException;

public enum OrderEvent {
    CONFIRM_PAYMENT {
        @Override
        public void nextState(CustomerOrder customerOrder) {
            if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RECYCLED)) {
                customerOrder.setOrderState(OrderState.PAID_RECYCLED);
            } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RESERVED)) {
                customerOrder.setOrderState(OrderState.PAID_RESERVED);
            } else {
                throw new StateChangeException();
            }
        }
    },
    RECYCLE_ORDER_STORAGE {
        @Override
        public void nextState(CustomerOrder customerOrder) {
            if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RESERVED)) {
                customerOrder.setOrderState(OrderState.NOT_PAID_RECYCLED);
            } else {
                throw new StateChangeException();
            }
        }
    },
    DECREASE_ACTUAL_STORAGE {
        @Override
        public void nextState(CustomerOrder customerOrder) {
            if (customerOrder.getOrderState().equals(OrderState.PAID_RESERVED)) {
                customerOrder.setOrderState(OrderState.CONFIRMED);
            } else {
                throw new StateChangeException();
            }
        }
    },
    RESERVE {
        @Override
        public void nextState(CustomerOrder customerOrder) {
            if (customerOrder.getOrderState().equals(OrderState.PAID_RECYCLED)) {
                customerOrder.setOrderState(OrderState.PAID_RESERVED);
            } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RECYCLED)) {
                customerOrder.setOrderState(OrderState.NOT_PAID_RESERVED);
            } else {
                throw new StateChangeException();
            }
        }
    };

    public abstract void nextState(CustomerOrder customerOrder);
}
