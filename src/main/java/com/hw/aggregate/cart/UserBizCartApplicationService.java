package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.UserCreateBizCartItemCommand;
import com.hw.aggregate.cart.model.BizCartItem;
import com.hw.aggregate.cart.representation.UserBizCartItemCardRep;
import com.hw.shared.rest.RoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
@Service
public class UserBizCartApplicationService extends RoleBasedRestfulService<BizCartItem, UserBizCartItemCardRep, Void, VoidTypedClass> {
    {
        entityClass = BizCartItem.class;
        role = RestfulQueryRegistry.RoleEnum.USER;
    }

    @Override
    public UserBizCartItemCardRep getEntitySumRepresentation(BizCartItem bizCart) {
        return new UserBizCartItemCardRep(bizCart);
    }

    @Override
    protected BizCartItem createEntity(long id, Object command) {
        return BizCartItem.create(id, (UserCreateBizCartItemCommand) command, this);
    }

}
