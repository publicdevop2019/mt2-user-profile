package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.CustomerOrderAddressCommand;
import lombok.Data;

@Data
public class PlaceOrderAgainCommand {
    private CustomerOrderAddressCommand address;
}
