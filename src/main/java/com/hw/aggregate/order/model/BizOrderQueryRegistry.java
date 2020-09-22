package com.hw.aggregate.order.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
@Component
public class BizOrderQueryRegistry extends RestfulQueryRegistry<BizOrder> {
    @Autowired
    private AdminBizOrderSelectQueryBuilder adminBizOrderSelectQueryBuilder;
    @Autowired
    private UserBizOrderDeleteQueryBuilder userBizOrderDeleteQueryBuilder;
    @Autowired
    private UserBizOrderSelectQueryBuilder userBizOrderSelectQueryBuilder;

    @Override
    @PostConstruct
    protected void configQueryBuilder() {
        selectQueryBuilder.put(RoleEnum.USER, userBizOrderSelectQueryBuilder);
        selectQueryBuilder.put(RoleEnum.ADMIN, adminBizOrderSelectQueryBuilder);
        deleteQueryBuilder.put(RoleEnum.USER, userBizOrderDeleteQueryBuilder);
    }
}
