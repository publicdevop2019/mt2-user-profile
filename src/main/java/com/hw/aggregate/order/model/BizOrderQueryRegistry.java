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
    @Autowired
    private AppBizOrderSelectQueryBuilder appBizOrderSelectQueryBuilder;

    @Override
    @PostConstruct
    protected void configQueryBuilder() {
        selectQueryBuilder.put(RoleEnum.USER, userBizOrderSelectQueryBuilder);
        selectQueryBuilder.put(RoleEnum.ADMIN, adminBizOrderSelectQueryBuilder);
        selectQueryBuilder.put(RoleEnum.APP, appBizOrderSelectQueryBuilder);
        deleteQueryBuilder.put(RoleEnum.USER, userBizOrderDeleteQueryBuilder);
    }
}
