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

    @Override
    @PostConstruct
    protected void configQueryBuilder() {
        selectQueryBuilder.put(RoleEnum.USER, userBizCartItemSelectQueryBuilder);
        deleteQueryBuilder.put(RoleEnum.USER, userBizCartItemDeleteQueryBuilder);
    }
}
