package com.hw.aggregate.order.command;

import lombok.Data;

@Data
public class DeleteOrderCustomerCommand {
    private Long orderId;

    public DeleteOrderCustomerCommand(Long orderId) {
        this.orderId = orderId;
    }
}
