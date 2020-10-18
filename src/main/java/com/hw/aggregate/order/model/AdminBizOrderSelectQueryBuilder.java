package com.hw.aggregate.order.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.stereotype.Component;
@Component
public class AdminBizOrderSelectQueryBuilder extends SelectQueryBuilder<BizOrder> {
    AdminBizOrderSelectQueryBuilder(){
        allowEmptyClause=true;
    }
}
