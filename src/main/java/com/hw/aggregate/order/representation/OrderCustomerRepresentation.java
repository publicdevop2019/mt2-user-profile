package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderAddress;
import com.hw.aggregate.order.model.CustomerOrderItem;
import com.hw.aggregate.order.model.OrderState;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class OrderCustomerRepresentation {

    private CustomerOrderAddress address;

    private ArrayList<CustomerOrderItem> productList;

    private String paymentType;

    private OrderState orderState;

    private BigDecimal paymentAmt;

    public OrderCustomerRepresentation(CustomerOrder customerOrder) {
        this.address = customerOrder.getAddress();
        this.productList = customerOrder.getReadOnlyProductList();
        this.paymentType = customerOrder.getPaymentType();
        this.paymentAmt = customerOrder.getPaymentAmt();
        this.orderState = customerOrder.getOrderState();
    }
}
