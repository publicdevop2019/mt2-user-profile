package com.hw.aggregate.order.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserBizOrderSelectQueryBuilder extends SelectQueryBuilder<BizOrder> {
    {
        allowEmptyClause = true;
        defaultWhereField.add(new UserIdClause());
    }
}
