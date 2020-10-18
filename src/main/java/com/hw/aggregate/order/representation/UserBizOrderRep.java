package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.command.BizOrderAddressCmdRep;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderAddress;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class UserBizOrderRep {
    private Long id;
    private BizOrderAddressCmdRep address;

    private ArrayList<BizOrderItem> productList;

    private String paymentType;

    private BizOrderStatus orderState;

    private BigDecimal paymentAmt;
    private String paymentLink;
    private boolean paid;
    private Integer version;

    public UserBizOrderRep(BizOrder customerOrder) {
        this.id = customerOrder.getId();
        this.productList = customerOrder.getReadOnlyProductList();
        this.paymentType = customerOrder.getPaymentType();
        this.paymentLink = customerOrder.getPaymentLink();
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
        this.paid=customerOrder.isPaid();
        this.version=customerOrder.getVersion();

    }
}
