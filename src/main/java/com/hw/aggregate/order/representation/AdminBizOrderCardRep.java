package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
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
    private Integer version;

    public AdminBizOrderCardRep(BizOrder bizOrder) {
        BeanUtils.copyProperties(bizOrder, this);
        this.productList = bizOrder.getReadOnlyProductList();
        this.createdAt = bizOrder.getCreatedAt().getTime();
    }
}
