package com.hw.aggregate.address.model;

import com.hw.shared.sql.builder.DeleteByIdQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizAddressDeleteQueryBuilder extends DeleteByIdQueryBuilder<BizAddress> {
    UserBizAddressDeleteQueryBuilder() {
        defaultWhereField.add(new CreatedByClause());
    }
    @Autowired
    private void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }
}
