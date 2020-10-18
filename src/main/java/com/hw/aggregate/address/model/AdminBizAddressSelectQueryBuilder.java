package com.hw.aggregate.address.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.stereotype.Component;
@Component
public class AdminBizAddressSelectQueryBuilder extends SelectQueryBuilder<BizAddress> {
    AdminBizAddressSelectQueryBuilder(){
        allowEmptyClause=true;
    }
}
