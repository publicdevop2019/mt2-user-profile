package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.CustomerOrderAddressCmdRep;
import com.hw.aggregate.order.model.CustomerOrderItemCommand;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderCommand {
    private CustomerOrderAddressCmdRep address;
    private List<CustomerOrderItemCommand> productList;
    private String paymentType;
    private BigDecimal paymentAmt;
}
