package com.hw.aggregate.order.command;

public class ConfirmOrderPaymentCommand {
    public Long orderId;

    public ConfirmOrderPaymentCommand(Long orderId) {
        this.orderId = orderId;
    }
}
