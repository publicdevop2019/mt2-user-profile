package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class OrderCustomerRepresentation {
    private Long id;
    private CustomerOrderAddressCmdRep address;

    private ArrayList<CustomerOrderItem> productList;

    private String paymentType;

    private OrderStatus orderState;

    private BigDecimal paymentAmt;

    public OrderCustomerRepresentation(CustomerOrder customerOrder) {
        this.id = customerOrder.getId();
        this.productList = customerOrder.getReadOnlyProductList();
        this.paymentType = customerOrder.getPaymentType();
        this.paymentAmt = customerOrder.getPaymentAmt();
        this.orderState = customerOrder.getOrderState();
        CustomerOrderAddress address = customerOrder.getAddress();
        this.address = new CustomerOrderAddressCmdRep();
        this.address.setCountry(address.getOrderAddressCountry());
        this.address.setProvince(address.getOrderAddressProvince());
        this.address.setCity(address.getOrderAddressCity());
        this.address.setPostalCode(address.getOrderAddressPostalCode());
        this.address.setLine1(address.getOrderAddressLine1());
        this.address.setLine2(address.getOrderAddressLine2());
        this.address.setPhoneNumber(address.getOrderAddressPhoneNumber());
        this.address.setFullName(address.getOrderAddressFullName());

    }
}
