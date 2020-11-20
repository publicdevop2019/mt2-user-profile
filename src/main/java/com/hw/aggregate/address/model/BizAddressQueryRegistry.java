package com.hw.aggregate.address.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Component;

@Component
public class BizAddressQueryRegistry extends RestfulQueryRegistry<BizAddress> {
    @Override
    public Class<BizAddress> getEntityClass() {
        return BizAddress.class;
    }
    private void setUp() {
        cacheable.put(RoleEnum.USER, true);
        cacheable.put(RoleEnum.ADMIN, true);
        cacheable.put(RoleEnum.APP, true);
        cacheable.put(RoleEnum.PUBLIC, true);
        cacheable.put(RoleEnum.ROOT, true);
    }
}
