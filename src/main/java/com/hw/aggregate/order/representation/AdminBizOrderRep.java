package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderAddress;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class AdminBizOrderRep {
    private Long userId;
    private Long id;

    private BizOrderAddress address;
    private ArrayList<BizOrderItem> productList;
    private String paymentType;
    private String paymentLink;
    private BigDecimal paymentAmt;
    private String paymentDate;
    private boolean paid;
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
        this.paid = bizOrder.isPaid();
        this.orderState = bizOrder.getOrderState();
        this.modifiedByUserAt = bizOrder.getModifiedByUserAt().getTime();
        this.createdBy = bizOrder.getCreatedBy();
        this.createdAt = bizOrder.getCreatedAt().getTime();
        this.modifiedBy = bizOrder.getModifiedBy();
        this.modifiedAt = bizOrder.getModifiedAt().getTime();
        this.userId = bizOrder.getUserId();
    }
}
