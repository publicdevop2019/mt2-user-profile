package com.hw.aggregate.address.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserBizAddressSelectQueryBuilder extends SelectQueryBuilder<BizAddress> {
    UserBizAddressSelectQueryBuilder() {
        defaultWhereField.add(new CreatedByClause());
        allowEmptyClause = true;
    }

}
