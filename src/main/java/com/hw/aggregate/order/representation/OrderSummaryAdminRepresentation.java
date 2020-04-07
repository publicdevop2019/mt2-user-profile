package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;

import java.util.ArrayList;
import java.util.List;

public class OrderSummaryAdminRepresentation extends ArrayList<CustomerOrder> {
    public List<CustomerOrder> customerOrders;

    public OrderSummaryAdminRepresentation(List<CustomerOrder> collect) {
        customerOrders = collect;
    }
}
