package com.hw.aggregate.cart.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizCartItemSelectQueryBuilder extends SelectQueryBuilder<BizCartItem> {
    UserBizCartItemSelectQueryBuilder() {
        allowEmptyClause = true;
        defaultWhereField.add(new CreatedByClause());
    }

}
