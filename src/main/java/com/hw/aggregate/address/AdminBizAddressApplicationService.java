package com.hw.aggregate.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.address.model.BizAddress;
import com.hw.aggregate.address.model.BizAddressQueryRegistry;
import com.hw.aggregate.address.representation.AdminBizAddressCardRep;
import com.hw.aggregate.address.representation.AdminBizAddressRep;
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
public class AdminBizAddressApplicationService extends DefaultRoleBasedRestfulService<BizAddress, AdminBizAddressCardRep, AdminBizAddressRep, VoidTypedClass> {

    @Autowired
    private BizAddressRepository repo2;
    @Autowired
    private AppChangeRecordApplicationService changeHistoryRepository;

    @Autowired
    private IdGenerator idGenerator2;

    @Autowired
    private BizAddressQueryRegistry skuQueryRegistry;

    @Autowired
    private ObjectMapper om2;

    @PostConstruct
    private void setUp() {
        repo = repo2;
        idGenerator = idGenerator2;
        queryRegistry = skuQueryRegistry;
        entityClass = BizAddress.class;
        role = RestfulQueryRegistry.RoleEnum.ADMIN;
        om = om2;
        appChangeRecordApplicationService = changeHistoryRepository;
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
