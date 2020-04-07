package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;

public class OrderRepresentation {
    public CustomerOrder customerOrder;

    public OrderRepresentation(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }
}
