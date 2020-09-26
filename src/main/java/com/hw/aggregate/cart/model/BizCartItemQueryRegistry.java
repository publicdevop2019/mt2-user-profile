package com.hw.aggregate.cart.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BizCartItemQueryRegistry extends RestfulQueryRegistry<BizCartItem> {
    @Autowired
    private UserBizCartItemSelectQueryBuilder userBizCartItemSelectQueryBuilder;
    @Autowired
    private UserBizCartItemDeleteQueryBuilder userBizCartItemDeleteQueryBuilder;
    @Autowired
    private AppBizCartItemDeleteQueryBuilder appBizCartItemDeleteQueryBuilder;
    @Autowired
    private AppBizCartItemSelectQueryBuilder appBizCartItemSelectQueryBuilder;

    @Override
    @PostConstruct
    protected void configQueryBuilder() {
        selectQueryBuilder.put(RoleEnum.USER, userBizCartItemSelectQueryBuilder);
        selectQueryBuilder.put(RoleEnum.APP, appBizCartItemSelectQueryBuilder);
        deleteQueryBuilder.put(RoleEnum.USER, userBizCartItemDeleteQueryBuilder);
        deleteQueryBuilder.put(RoleEnum.APP, appBizCartItemDeleteQueryBuilder);
    }
}
