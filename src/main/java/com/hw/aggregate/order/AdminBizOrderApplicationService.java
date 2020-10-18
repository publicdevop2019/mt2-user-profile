package com.hw.aggregate.order;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.representation.AdminBizOrderCardRep;
import com.hw.aggregate.order.representation.AdminBizOrderRep;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class AdminBizOrderApplicationService extends DefaultRoleBasedRestfulService<BizOrder, AdminBizOrderCardRep, AdminBizOrderRep, VoidTypedClass> {

    @PostConstruct
    private void setUp() {
        entityClass = BizOrder.class;
        role = RestfulQueryRegistry.RoleEnum.ADMIN;
    }

    @Override
    public BizOrder replaceEntity(BizOrder bizOrder, Object command) {
        return null;
    }

    @Override
    public AdminBizOrderCardRep getEntitySumRepresentation(BizOrder bizOrder) {
        return new AdminBizOrderCardRep(bizOrder);
    }

    @Override
    public AdminBizOrderRep getEntityRepresentation(BizOrder bizOrder) {
        return new AdminBizOrderRep(bizOrder);
    }

    @Override
    protected BizOrder createEntity(long id, Object command) {
        return null;
    }

    @Override
    public void preDelete(BizOrder bizOrder) {

    }

    @Override
    public void postDelete(BizOrder bizOrder) {

    }

    @Override
    protected void prePatch(BizOrder bizOrder, Map<String, Object> params, VoidTypedClass middleLayer) {

    }

    @Override
    protected void postPatch(BizOrder bizOrder, Map<String, Object> params, VoidTypedClass middleLayer) {

    }
}
