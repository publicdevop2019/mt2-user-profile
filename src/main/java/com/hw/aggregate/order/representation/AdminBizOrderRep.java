package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
@Data
public class AdminBizOrderRep {
    private Long id;

    private BizOrderAddress address;
    private ArrayList<BizOrderItem> productList;
    private String paymentType;
    private String paymentLink;
    private BigDecimal paymentAmt;
    private String paymentDate;
    private Boolean paid;
    private BizOrderStatus orderState;
    private long modifiedByUserAt;
    private String createdBy;
    private long createdAt;
    private String modifiedBy;
    private long modifiedAt;

    public AdminBizOrderRep(BizOrder bizOrder) {
        this.id = bizOrder.getId();
        this.address = bizOrder.getAddress();
        this.productList = bizOrder.getReadOnlyProductList();
        this.paymentType = bizOrder.getPaymentType();
        this.paymentLink = bizOrder.getPaymentLink();
        this.paymentAmt = bizOrder.getPaymentAmt();
        this.paymentDate = bizOrder.getPaymentDate();
        this.paid = bizOrder.getPaid();
        this.orderState = bizOrder.getOrderState();
        this.modifiedByUserAt = bizOrder.getModifiedByUserAt().getTime();
        this.createdBy = bizOrder.getCreatedBy();
        this.createdAt = bizOrder.getCreatedAt().getTime();
        this.modifiedBy = bizOrder.getModifiedBy();
        this.modifiedAt = bizOrder.getModifiedAt().getTime();
    }
}
