package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.CustomerOrderAddressCmdRep;
import lombok.Data;

@Data
public class PlaceOrderAgainCommand {
    private CustomerOrderAddressCmdRep address;
}
