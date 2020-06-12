package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class OrderCustomerRepresentation {
    private Long id;
    private BizOrderAddressCmdRep address;

    private ArrayList<BizOrderItem> productList;

    private String paymentType;

    private BizOrderStatus orderState;

    private BigDecimal paymentAmt;

    public OrderCustomerRepresentation(BizOrder customerOrder) {
        this.id = customerOrder.getId();
        this.productList = customerOrder.getReadOnlyProductList();
        this.paymentType = customerOrder.getPaymentType();
        this.paymentAmt = customerOrder.getPaymentAmt();
        this.orderState = customerOrder.getOrderState();
        BizOrderAddress address = customerOrder.getAddress();
        this.address = new BizOrderAddressCmdRep();
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
