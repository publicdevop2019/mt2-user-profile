package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class BizOrderSummaryCustomerRepresentationTest {
    @Test
    public void getOrderSummaryCustomerRepresentationTest() {
        ArrayList<BizOrder> customerOrders = new ArrayList<>();
        BizOrderSummaryCustomerRepresentation orderSummaryAdminRepresentation = new BizOrderSummaryCustomerRepresentation(customerOrders);
        Assert.assertEquals(0, orderSummaryAdminRepresentation.getOrderList().size());
    }
    @Test
    public void getOrderSummaryCustomerRepresentationTest_2() {
        ArrayList<BizOrder> customerOrders = new ArrayList<>();
        customerOrders.add(new BizOrder());
        BizOrderSummaryCustomerRepresentation orderSummaryAdminRepresentation = new BizOrderSummaryCustomerRepresentation(customerOrders);
        Assert.assertEquals(1, orderSummaryAdminRepresentation.getOrderList().size());
    }
}