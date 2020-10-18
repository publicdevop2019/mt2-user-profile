package com.hw.aggregate.cart.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Component;

@Component
public class BizCartItemQueryRegistry extends RestfulQueryRegistry<BizCartItem> {
    @Override
    public Class<BizCartItem> getEntityClass() {
        return BizCartItem.class;
    }
}
