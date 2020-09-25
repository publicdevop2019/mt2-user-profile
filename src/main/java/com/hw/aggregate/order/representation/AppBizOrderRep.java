package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import lombok.Data;

import java.util.ArrayList;
@Data
public class AppBizOrderRep {
    private ArrayList<BizOrderItem> readOnlyProductList;

    public AppBizOrderRep(BizOrder bizOrder) {
        this.readOnlyProductList = bizOrder.getReadOnlyProductList();
    }
}
