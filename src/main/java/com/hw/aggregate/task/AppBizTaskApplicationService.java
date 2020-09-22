package com.hw.aggregate.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.task.command.AppCreateBizTaskCommand;
import com.hw.aggregate.task.command.AppUpdateBizTaskCommand;
import com.hw.aggregate.task.model.BizTask;
import com.hw.aggregate.task.model.BizTaskQueryRegistry;
import com.hw.aggregate.task.representation.AppBizTaskRep;
import com.hw.shared.IdGenerator;
import com.hw.shared.idempotent.AppChangeRecordApplicationService;
import com.hw.shared.idempotent.OperationType;
import com.hw.shared.rest.CreatedEntityRep;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import com.hw.shared.sql.SumPagedRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class AppBizTaskApplicationService extends DefaultRoleBasedRestfulService<BizTask, Void, AppBizTaskRep, VoidTypedClass> {
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
        role = RestfulQueryRegistry.RoleEnum.APP;
        idGenerator = idGenerator2;
        appChangeRecordApplicationService = changeRepository2;
        om = objectMapper;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreatedEntityRep create(Object command, String changeId) {
        return super.create(command,changeId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void replaceById(Long id, Object command, String changeId) {
        super.replaceById(id,command,changeId);
    }

    @Override
    public BizTask replaceEntity(BizTask bizTask, Object command) {
        return bizTask.replace((AppUpdateBizTaskCommand) command);
    }

    @Override
    public Void getEntitySumRepresentation(BizTask bizTask) {
        return null;
    }

    @Override
    public AppBizTaskRep getEntityRepresentation(BizTask bizTask) {
        return new AppBizTaskRep(bizTask);
    }

    @Override
    protected BizTask createEntity(long id, Object command) {
        return BizTask.create(id, (AppCreateBizTaskCommand) command);
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
