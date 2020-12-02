package com.hw.aggregate.order.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Component;

@Component
public class BizOrderQueryRegistry extends RestfulQueryRegistry<BizOrder> {
    @Override
    public Class<BizOrder> getEntityClass() {
        return BizOrder.class;
    }
}
