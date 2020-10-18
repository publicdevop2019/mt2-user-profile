package com.hw.aggregate.address.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Component;

@Component
public class BizAddressQueryRegistry extends RestfulQueryRegistry<BizAddress> {
    @Override
    public Class<BizAddress> getEntityClass() {
        return BizAddress.class;
    }
}
