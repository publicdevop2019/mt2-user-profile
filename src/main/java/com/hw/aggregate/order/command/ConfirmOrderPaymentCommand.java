package com.hw.aggregate.order.command;

import lombok.Data;

@Data
public class ConfirmOrderPaymentCommand {
    private Long orderId;

    public ConfirmOrderPaymentCommand(Long orderId) {
        this.orderId = orderId;
    }
}
