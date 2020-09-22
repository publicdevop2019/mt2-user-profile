package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserBizOrderCardRep {
    private Long id;
    private BigDecimal paymentAmt;
    private BizOrderStatus orderState;
    private List<BizOrderItem> productList;

    public UserBizOrderCardRep(BizOrder customerOrder) {
        this.id = customerOrder.getId();
        this.paymentAmt = customerOrder.getPaymentAmt();
        this.orderState = customerOrder.getOrderState();
        this.productList = customerOrder.getReadOnlyProductList();
    }
}
