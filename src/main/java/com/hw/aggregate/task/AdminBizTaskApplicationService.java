package com.hw.aggregate.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.task.model.BizTask;
import com.hw.aggregate.task.model.BizTaskQueryRegistry;
import com.hw.aggregate.task.representation.AdminBizTaskCardRep;
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
public class AdminBizTaskApplicationService extends DefaultRoleBasedRestfulService<BizTask, AdminBizTaskCardRep, Void, VoidTypedClass> {
    @Autowired
    private BizTaskQueryRegistry registry;
    @Autowired
    private IdGenerator idGenerator2;
    @Autowired
    private AppChangeRecordApplicationService changeRepository2;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BizTaskRepository repo2;


    @PostConstruct
    private void setUp() {
        repo = repo2;
        queryRegistry = registry;
        entityClass = BizTask.class;
        role = RestfulQueryRegistry.RoleEnum.ADMIN;
        idGenerator = idGenerator2;
        appChangeRecordApplicationService = changeRepository2;
        om = objectMapper;
    }

    @Override
    public BizTask replaceEntity(BizTask bizTask, Object command) {
        return null;
    }

    @Override
    public AdminBizTaskCardRep getEntitySumRepresentation(BizTask bizTask) {
        return new AdminBizTaskCardRep(bizTask);
    }

    @Override
    public Void getEntityRepresentation(BizTask bizTask) {
        return null;
    }

    @Override
    protected BizTask createEntity(long id, Object command) {
        return null;
    }

    @Override
    public void preDelete(BizTask bizTask) {

    }

    @Override
    public void postDelete(BizTask bizTask) {

    }

    @Override
    protected void prePatch(BizTask bizTask, Map<String, Object> params, VoidTypedClass middleLayer) {

    }

    @Override
    protected void postPatch(BizTask bizTask, Map<String, Object> params, VoidTypedClass middleLayer) {

    }
}
