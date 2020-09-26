package com.hw.aggregate.cart.model;

import com.hw.shared.sql.builder.DeleteByIdQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizCartItemDeleteQueryBuilder extends DeleteByIdQueryBuilder<BizCartItem> {
    UserBizCartItemDeleteQueryBuilder() {
        defaultWhereField.add(new CreatedByClause());
    }

    @Autowired
    private void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }
}
