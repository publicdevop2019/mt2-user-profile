package com.hw.aggregate.cart;

import com.hw.aggregate.cart.model.BizCartItem;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class AppBizCartApplicationService extends DefaultRoleBasedRestfulService<BizCartItem, Void, Void, VoidTypedClass> {

    @PostConstruct
    private void setUp() {
        entityClass = BizCartItem.class;
        role = RestfulQueryRegistry.RoleEnum.APP;
    }

    @Override
    public BizCartItem replaceEntity(BizCartItem bizCart, Object command) {
        return null;
    }

    @Override
    public Void getEntitySumRepresentation(BizCartItem bizCart) {
        return null;
    }

    @Override
    public Void getEntityRepresentation(BizCartItem bizCart) {
        return null;
    }

    @Override
    protected BizCartItem createEntity(long id, Object command) {
        return null;
    }

    @Override
    public void preDelete(BizCartItem bizCart) {

    }

    @Override
    public void postDelete(BizCartItem bizCart) {

    }

    @Override
    protected void prePatch(BizCartItem bizCart, Map<String, Object> params, VoidTypedClass middleLayer) {

    }

    @Override
    protected void postPatch(BizCartItem bizCart, Map<String, Object> params, VoidTypedClass middleLayer) {

    }
}
