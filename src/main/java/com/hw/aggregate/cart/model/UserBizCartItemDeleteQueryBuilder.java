package com.hw.aggregate.cart.model;

import com.hw.shared.sql.builder.SoftDeleteQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizCartItemDeleteQueryBuilder extends SoftDeleteQueryBuilder<BizCartItem> {
    UserBizCartItemDeleteQueryBuilder() {
        defaultWhereField.add(new CreatedByClause());
    }

}
