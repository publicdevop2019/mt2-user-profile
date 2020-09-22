package com.hw.aggregate.address.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizAddressSelectQueryBuilder extends SelectQueryBuilder<BizAddress> {
    UserBizAddressSelectQueryBuilder() {
        defaultWhereField.add(new CreatedByClause());
        allowEmptyClause = true;
    }

    @Autowired
    private void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }
}
