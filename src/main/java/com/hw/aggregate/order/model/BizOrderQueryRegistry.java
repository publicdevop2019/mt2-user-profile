package com.hw.aggregate.order.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Component;

@Component
public class BizOrderQueryRegistry extends RestfulQueryRegistry<BizOrder> {
    @Override
    public Class<BizOrder> getEntityClass() {
        return BizOrder.class;
    }
    private void setUp() {
        cacheable.put(RoleEnum.USER, true);
        cacheable.put(RoleEnum.ADMIN, true);
        cacheable.put(RoleEnum.APP, true);
        cacheable.put(RoleEnum.PUBLIC, true);
        cacheable.put(RoleEnum.ROOT, true);
    }
}
