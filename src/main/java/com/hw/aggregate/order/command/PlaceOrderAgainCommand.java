package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.CustomerOrderAddress;
import lombok.Data;

@Data
public class PlaceOrderAgainCommand {
    private CustomerOrderAddress address;
    private String paymentType;
}
