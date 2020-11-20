package com.hw.aggregate.cart.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Component;

@Component
public class BizCartItemQueryRegistry extends RestfulQueryRegistry<BizCartItem> {
    @Override
    public Class<BizCartItem> getEntityClass() {
        return BizCartItem.class;
    }
    private void setUp() {
        cacheable.put(RoleEnum.USER, true);
        cacheable.put(RoleEnum.ADMIN, true);
        cacheable.put(RoleEnum.APP, true);
        cacheable.put(RoleEnum.PUBLIC, true);
        cacheable.put(RoleEnum.ROOT, true);
    }
}
