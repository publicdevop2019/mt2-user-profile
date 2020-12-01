package com.hw.aggregate.order.model;

import com.hw.shared.sql.builder.SoftDeleteQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserBizOrderDeleteQueryBuilder extends SoftDeleteQueryBuilder<BizOrder> {
    {
        defaultWhereField.add(new UserIdClause());
        defaultWhereField.add(new NotPayedClause());
    }
}
