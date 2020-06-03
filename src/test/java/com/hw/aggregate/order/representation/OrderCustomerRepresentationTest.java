package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;
import org.junit.Assert;
import org.junit.Test;

import static com.hw.aggregate.Helper.rLong;

public class OrderCustomerRepresentationTest {

    @Test
    public void setOrderState() {
        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setId(rLong());
        OrderCustomerRepresentation orderCustomerRepresentation = new OrderCustomerRepresentation(customerOrder);
        Assert.assertEquals(customerOrder.getId(), orderCustomerRepresentation.getId());
    }
}