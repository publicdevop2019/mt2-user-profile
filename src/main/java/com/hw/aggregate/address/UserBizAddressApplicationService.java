package com.hw.aggregate.address;

import com.hw.aggregate.address.command.UserCreateBizAddressCommand;
import com.hw.aggregate.address.command.UserUpdateBizAddressCommand;
import com.hw.aggregate.address.model.BizAddress;
import com.hw.aggregate.address.representation.UserBizAddressCardRep;
import com.hw.aggregate.address.representation.UserBizAddressRep;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Slf4j
public class UserBizAddressApplicationService extends DefaultRoleBasedRestfulService<BizAddress, UserBizAddressCardRep, UserBizAddressRep, VoidTypedClass> {

    @PostConstruct
    private void setUp() {
        entityClass = BizAddress.class;
        role = RestfulQueryRegistry.RoleEnum.USER;
        deleteHook = true;
    }

    @Override
    public BizAddress replaceEntity(BizAddress bizAddress, Object command) {
        bizAddress.replace((UserUpdateBizAddressCommand) command);
        return bizAddress;
    }

    @Override
    public UserBizAddressCardRep getEntitySumRepresentation(BizAddress bizAddress) {
        return new UserBizAddressCardRep(bizAddress);
    }

    @Override
    public UserBizAddressRep getEntityRepresentation(BizAddress bizAddress) {
        return new UserBizAddressRep(bizAddress);
    }

    @Override
    protected BizAddress createEntity(long id, Object command) {
        return BizAddress.create(id, (UserCreateBizAddressCommand) command, this);
    }

    @Override
    public void preDelete(BizAddress bizAddress) {

    }

    @Override
    public void postDelete(BizAddress bizAddress) {

    }

    @Override
    protected void prePatch(BizAddress bizAddress, Map<String, Object> params, VoidTypedClass middleLayer) {

    }

    @Override
    protected void postPatch(BizAddress bizAddress, Map<String, Object> params, VoidTypedClass middleLayer) {

    }
}
