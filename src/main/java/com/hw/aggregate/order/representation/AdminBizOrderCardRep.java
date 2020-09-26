package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class AdminBizOrderCardRep {
    private Long id;
    private BigDecimal paymentAmt;
    private BizOrderStatus orderState;
    private List<BizOrderItem> productList;
    private long createdAt;
    private long userId;
    private String createdBy;

    public AdminBizOrderCardRep(BizOrder bizOrder) {

        this.id = bizOrder.getId();
        this.productList = bizOrder.getReadOnlyProductList();
        this.paymentAmt = bizOrder.getPaymentAmt();
        this.orderState = bizOrder.getOrderState();
        this.createdAt = bizOrder.getCreatedAt().getTime();
        this.createdBy = bizOrder.getCreatedBy();
        this.userId = bizOrder.getUserId();
    }
}
