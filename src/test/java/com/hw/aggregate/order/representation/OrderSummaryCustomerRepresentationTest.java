package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class OrderSummaryCustomerRepresentationTest {
    @Test
    public void getOrderSummaryCustomerRepresentationTest() {
        ArrayList<BizOrder> customerOrders = new ArrayList<>();
        OrderSummaryCustomerRepresentation orderSummaryAdminRepresentation = new OrderSummaryCustomerRepresentation(customerOrders);
        Assert.assertEquals(0, orderSummaryAdminRepresentation.getOrderList().size());
    }
    @Test
    public void getOrderSummaryCustomerRepresentationTest_2() {
        ArrayList<BizOrder> customerOrders = new ArrayList<>();
        customerOrders.add(new BizOrder());
        OrderSummaryCustomerRepresentation orderSummaryAdminRepresentation = new OrderSummaryCustomerRepresentation(customerOrders);
        Assert.assertEquals(1, orderSummaryAdminRepresentation.getOrderList().size());
    }
}