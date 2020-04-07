package com.hw.aggregate.order.command;

public class DeleteOrderCustomerCommand {
    public Long orderId;

    public DeleteOrderCustomerCommand(Long orderId) {
        this.orderId = orderId;
    }
}
