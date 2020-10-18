package com.hw.aggregate.order.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class AppBizOrderSelectQueryBuilder extends SelectQueryBuilder<BizOrder> {
    AppBizOrderSelectQueryBuilder(){
        allowEmptyClause=false;
    }
}
