package com.hw.aggregate.cart.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserBizCartItemSelectQueryBuilder extends SelectQueryBuilder<BizCartItem> {
    {
        allowEmptyClause = true;
        defaultWhereField.add(new CreatedByClause());
    }
}
