package com.hw.aggregate.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.cart.command.UserCreateBizCartItemCommand;
import com.hw.aggregate.cart.model.BizCartItem;
import com.hw.aggregate.cart.model.BizCartItemQueryRegistry;
import com.hw.aggregate.cart.representation.UserBizCartItemCardRep;
import com.hw.shared.IdGenerator;
import com.hw.shared.idempotent.AppChangeRecordApplicationService;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
@Service
public class UserBizCartApplicationService extends DefaultRoleBasedRestfulService<BizCartItem, UserBizCartItemCardRep, Void, VoidTypedClass> {
    @Autowired
    private BizCartRepository repo2;
    @Autowired
    private AppChangeRecordApplicationService changeHistoryRepository;

    @Autowired
    private IdGenerator idGenerator2;

    @Autowired
    private BizCartItemQueryRegistry skuQueryRegistry;

    @Autowired
    private ObjectMapper om2;

    @PostConstruct
    private void setUp() {
        repo = repo2;
        idGenerator = idGenerator2;
        queryRegistry = skuQueryRegistry;
        entityClass = BizCartItem.class;
        role = RestfulQueryRegistry.RoleEnum.USER;
        om = om2;
        appChangeRecordApplicationService = changeHistoryRepository;
    }

    @Override
    public BizCartItem replaceEntity(BizCartItem bizCart, Object command) {
        return null;
    }

    @Override
    public UserBizCartItemCardRep getEntitySumRepresentation(BizCartItem bizCart) {
        return new UserBizCartItemCardRep(bizCart);
    }

    @Override
    public Void getEntityRepresentation(BizCartItem bizCart) {
        return null;
    }

    @Override
    protected BizCartItem createEntity(long id, Object command) {
        return BizCartItem.create(id, (UserCreateBizCartItemCommand) command, this);
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
