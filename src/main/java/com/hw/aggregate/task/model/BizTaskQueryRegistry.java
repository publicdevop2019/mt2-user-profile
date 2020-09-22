package com.hw.aggregate.task.model;

import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BizTaskQueryRegistry extends RestfulQueryRegistry<BizTask> {
    @Autowired
    private AppBizTaskSelectQueryBuilder appBizTaskSelectQueryBuilder;
    @Autowired
    private AdminBizTaskSelectQueryBuilder adminBizTaskSelectQueryBuilder;

    @Override
    @PostConstruct
    protected void configQueryBuilder() {
        selectQueryBuilder.put(RoleEnum.ADMIN, adminBizTaskSelectQueryBuilder);
        selectQueryBuilder.put(RoleEnum.APP, appBizTaskSelectQueryBuilder);
    }
}
