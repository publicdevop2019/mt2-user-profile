package com.hw.aggregate.address.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
@Component
public class AdminBizAddressSelectQueryBuilder extends SelectQueryBuilder<BizAddress> {
    AdminBizAddressSelectQueryBuilder(){
        allowEmptyClause=true;
    }
}
