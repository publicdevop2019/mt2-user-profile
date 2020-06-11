package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderAddress;
import org.junit.Assert;
import org.junit.Test;

import static com.hw.aggregate.Helper.rLong;

public class OrderCustomerRepresentationTest {

    @Test
    public void setOrderState() {
        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setId(rLong());
        CustomerOrderAddress customerOrderAddress = new CustomerOrderAddress();
        customerOrder.setAddress(customerOrderAddress);
        OrderCustomerRepresentation orderCustomerRepresentation = new OrderCustomerRepresentation(customerOrder);
        Assert.assertEquals(customerOrder.getId(), orderCustomerRepresentation.getId());
    }
}