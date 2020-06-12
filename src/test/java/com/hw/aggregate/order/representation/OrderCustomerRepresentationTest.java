package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderAddress;
import org.junit.Assert;
import org.junit.Test;

import static com.hw.aggregate.Helper.rLong;

public class OrderCustomerRepresentationTest {

    @Test
    public void setOrderState() {
        BizOrder customerOrder = new BizOrder();
        customerOrder.setId(rLong());
        BizOrderAddress customerOrderAddress = new BizOrderAddress();
        customerOrder.setAddress(customerOrderAddress);
        OrderCustomerRepresentation orderCustomerRepresentation = new OrderCustomerRepresentation(customerOrder);
        Assert.assertEquals(customerOrder.getId(), orderCustomerRepresentation.getId());
    }
}