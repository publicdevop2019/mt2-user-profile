package com.hw.aggregate.order.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
@Component
public class AdminBizOrderSelectQueryBuilder extends SelectQueryBuilder<BizOrder> {
    AdminBizOrderSelectQueryBuilder(){
        allowEmptyClause=true;
    }
}
