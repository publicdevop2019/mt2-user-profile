package com.hw.aggregate.order.model;

import com.hw.shared.sql.builder.DeleteByIdQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizOrderDeleteQueryBuilder extends DeleteByIdQueryBuilder<BizOrder> {
    UserBizOrderDeleteQueryBuilder(){
        defaultWhereField.add(new UserIdClause());
        defaultWhereField.add(new NotPayedClause());
    }
    @Autowired
    private void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }
}
