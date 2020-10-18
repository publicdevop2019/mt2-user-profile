package com.hw.aggregate.address.model;

import com.hw.shared.sql.builder.SoftDeleteQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserBizAddressDeleteQueryBuilder extends SoftDeleteQueryBuilder<BizAddress> {
    UserBizAddressDeleteQueryBuilder() {
        defaultWhereField.add(new CreatedByClause());
    }

}
