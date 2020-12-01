package com.hw.aggregate.order;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.representation.AdminBizOrderCardRep;
import com.hw.aggregate.order.representation.AdminBizOrderRep;
import com.hw.shared.rest.RoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class AdminBizOrderApplicationService extends RoleBasedRestfulService<BizOrder, AdminBizOrderCardRep, AdminBizOrderRep, VoidTypedClass> {
    {
        entityClass = BizOrder.class;
        role = RestfulQueryRegistry.RoleEnum.ADMIN;
    }

    @Override
    public AdminBizOrderCardRep getEntitySumRepresentation(BizOrder bizOrder) {
        return new AdminBizOrderCardRep(bizOrder);
    }

    @Override
    public AdminBizOrderRep getEntityRepresentation(BizOrder bizOrder) {
        return new AdminBizOrderRep(bizOrder);
    }

}
