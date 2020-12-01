package com.hw.aggregate.cart.model;

import com.hw.shared.sql.builder.SoftDeleteQueryBuilder;
import com.hw.shared.sql.clause.SelectFieldStringLikeClause;
import org.springframework.stereotype.Component;

import static com.hw.shared.Auditable.ENTITY_CREATED_BY;

@Component
public class AppBizCartItemDeleteQueryBuilder extends SoftDeleteQueryBuilder<BizCartItem> {
    {
        supportedWhereField.put(ENTITY_CREATED_BY, new SelectFieldStringLikeClause<>(ENTITY_CREATED_BY));
    }

}
