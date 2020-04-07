package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;

import java.util.List;

public class OrderSummaryCustomerRepresentation {
    public List<CustomerOrder> orderList;

    public OrderSummaryCustomerRepresentation(List<CustomerOrder> orderList) {
        this.orderList = orderList;

    }
}
