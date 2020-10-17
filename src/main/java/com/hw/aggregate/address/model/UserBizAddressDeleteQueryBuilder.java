package com.hw.aggregate.address.model;

import com.hw.shared.sql.builder.SoftDeleteQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class UserBizAddressDeleteQueryBuilder extends SoftDeleteQueryBuilder<BizAddress> {
    UserBizAddressDeleteQueryBuilder() {
        defaultWhereField.add(new CreatedByClause());
    }

}
