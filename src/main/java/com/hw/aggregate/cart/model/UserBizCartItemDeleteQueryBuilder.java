package com.hw.aggregate.cart.model;

import com.hw.shared.sql.builder.SoftDeleteQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserBizCartItemDeleteQueryBuilder extends SoftDeleteQueryBuilder<BizCartItem> {
    {
        defaultWhereField.add(new CreatedByClause());
    }
}
