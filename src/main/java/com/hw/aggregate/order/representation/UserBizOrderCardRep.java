package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserBizOrderCardRep {
    private Long id;
    private BigDecimal paymentAmt;
    private BizOrderStatus orderState;
    private List<BizOrderItem> productList;

    public UserBizOrderCardRep(BizOrder customerOrder) {
        BeanUtils.copyProperties(customerOrder, this);
        this.productList = customerOrder.getReadOnlyProductList();
    }
}
