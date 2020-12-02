package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderAddress;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;
import org.springframework.beans.BeanUtils;

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
    private Integer version;

    public AdminBizOrderRep(BizOrder bizOrder) {
        BeanUtils.copyProperties(bizOrder, this);
        this.productList = bizOrder.getReadOnlyProductList();
        this.modifiedByUserAt = bizOrder.getModifiedByUserAt().getTime();
        this.createdAt = bizOrder.getCreatedAt().getTime();
        this.modifiedAt = bizOrder.getModifiedAt().getTime();
    }
}
