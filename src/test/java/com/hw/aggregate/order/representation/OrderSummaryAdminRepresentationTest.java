package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class OrderSummaryAdminRepresentationTest {

    @Test
    public void getAdminRepresentations() {
        ArrayList<BizOrder> customerOrders = new ArrayList<>();
        OrderSummaryAdminRepresentation orderSummaryAdminRepresentation = new OrderSummaryAdminRepresentation(customerOrders);
        Assert.assertEquals(0, orderSummaryAdminRepresentation.getAdminRepresentations().size());
    }
    @Test
    public void getAdminRepresentations2() {
        ArrayList<BizOrder> customerOrders = new ArrayList<>();
        customerOrders.add(new BizOrder());
        OrderSummaryAdminRepresentation orderSummaryAdminRepresentation = new OrderSummaryAdminRepresentation(customerOrders);
        Assert.assertEquals(1, orderSummaryAdminRepresentation.getAdminRepresentations().size());
    }
}