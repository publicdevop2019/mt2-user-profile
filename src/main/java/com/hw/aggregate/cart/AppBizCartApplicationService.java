package com.hw.aggregate.cart;

import com.hw.aggregate.cart.model.BizCartItem;
import com.hw.shared.rest.RoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class AppBizCartApplicationService extends RoleBasedRestfulService<BizCartItem, Void, Void, VoidTypedClass> {
    {
        entityClass = BizCartItem.class;
        role = RestfulQueryRegistry.RoleEnum.APP;
    }
}
