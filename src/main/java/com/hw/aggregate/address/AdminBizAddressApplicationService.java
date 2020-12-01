package com.hw.aggregate.address;

import com.hw.aggregate.address.model.BizAddress;
import com.hw.aggregate.address.representation.AdminBizAddressCardRep;
import com.hw.aggregate.address.representation.AdminBizAddressRep;
import com.hw.shared.rest.RoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Service;

@Service
public class AdminBizAddressApplicationService extends RoleBasedRestfulService<BizAddress, AdminBizAddressCardRep, AdminBizAddressRep, VoidTypedClass> {
    {
        entityClass = BizAddress.class;
        role = RestfulQueryRegistry.RoleEnum.ADMIN;
    }

    @Override
    public AdminBizAddressCardRep getEntitySumRepresentation(BizAddress bizAddress) {
        return new AdminBizAddressCardRep(bizAddress);
    }

    @Override
    public AdminBizAddressRep getEntityRepresentation(BizAddress bizAddress) {
        return new AdminBizAddressRep(bizAddress);
    }

}
