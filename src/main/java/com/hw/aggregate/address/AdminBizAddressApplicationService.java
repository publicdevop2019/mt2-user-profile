package com.hw.aggregate.address;

import com.hw.aggregate.address.model.BizAddress;
import com.hw.aggregate.address.representation.AdminBizAddressCardRep;
import com.hw.aggregate.address.representation.AdminBizAddressRep;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
@Service
public class AdminBizAddressApplicationService extends DefaultRoleBasedRestfulService<BizAddress, AdminBizAddressCardRep, AdminBizAddressRep, VoidTypedClass> {

    @PostConstruct
    private void setUp() {
        entityClass = BizAddress.class;
        role = RestfulQueryRegistry.RoleEnum.ADMIN;
    }

    @Override
    public BizAddress replaceEntity(BizAddress bizAddress, Object command) {
        return null;
    }

    @Override
    public AdminBizAddressCardRep getEntitySumRepresentation(BizAddress bizAddress) {
        return new AdminBizAddressCardRep(bizAddress);
    }

    @Override
    public AdminBizAddressRep getEntityRepresentation(BizAddress bizAddress) {
        return new AdminBizAddressRep(bizAddress);
    }

    @Override
    protected BizAddress createEntity(long id, Object command) {
        return null;
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
