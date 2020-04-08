package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.CustomerOrderAddress;
import com.hw.aggregate.order.model.CustomerOrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReserveOrderCommand {
    private CustomerOrderAddress address;
    private List<CustomerOrderItem> productList;
    private String paymentType;
    private BigDecimal paymentAmt;
}
