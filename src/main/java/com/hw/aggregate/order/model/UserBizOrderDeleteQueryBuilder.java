package com.hw.aggregate.order.model;

import com.hw.shared.sql.builder.SoftDeleteQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizOrderDeleteQueryBuilder extends SoftDeleteQueryBuilder<BizOrder> {
    UserBizOrderDeleteQueryBuilder() {
        defaultWhereField.add(new UserIdClause());
        defaultWhereField.add(new NotPayedClause());
    }

}
