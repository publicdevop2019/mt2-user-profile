package com.hw.aggregate.address.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BizAddressQueryRegistry extends RestfulQueryRegistry<BizAddress> {
    @Autowired
    private AdminBizAddressSelectQueryBuilder adminBizAddressSelectQueryBuilder;
    @Autowired
    private UserBizAddressSelectQueryBuilder userBizAddressSelectQueryBuilder;
    @Autowired
    private UserBizAddressDeleteQueryBuilder userBizAddressDeleteQueryBuilder;

    @PostConstruct
    @Override
    protected void configQueryBuilder() {
        selectQueryBuilder.put(RoleEnum.ADMIN, adminBizAddressSelectQueryBuilder);
        selectQueryBuilder.put(RoleEnum.USER, userBizAddressSelectQueryBuilder);
        deleteQueryBuilder.put(RoleEnum.USER, userBizAddressDeleteQueryBuilder);
    }
}
