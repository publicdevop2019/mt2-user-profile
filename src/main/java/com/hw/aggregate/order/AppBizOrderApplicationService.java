package com.hw.aggregate.order;

import com.hw.aggregate.order.command.AppCreateBizOrderCommand;
import com.hw.aggregate.order.command.AppUpdateBizOrderCommand;
import com.hw.aggregate.order.command.AppValidateBizOrderCommand;
import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.aggregate.order.exception.VersionMismatchException;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.product.AppProductSumPagedRep;
import com.hw.aggregate.order.representation.AppBizOrderRep;
import com.hw.shared.idempotent.OperationType;
import com.hw.shared.idempotent.representation.AppChangeRecordCardRep;
import com.hw.shared.rest.CreatedEntityRep;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import com.hw.shared.sql.SumPagedRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.hw.shared.idempotent.model.ChangeRecord.CHANGE_ID;
import static com.hw.shared.idempotent.model.ChangeRecord.ENTITY_TYPE;

@Slf4j
@Service
public class AppBizOrderApplicationService extends DefaultRoleBasedRestfulService<BizOrder, Void, AppBizOrderRep, VoidTypedClass> {
    @Autowired
    private BizOrderRepository repo2;

    @Autowired
    private ProductService productService;

    @Autowired
    private SagaOrchestratorService sagaOrchestratorService;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @PostConstruct
    private void setUp() {
        entityClass = BizOrder.class;
        role = RestfulQueryRegistry.RoleEnum.APP;
    }

    @Transactional
    public void replaceById(Long id, Object command, String changeId) {
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            saveChangeRecord(command, changeId, OperationType.PUT, "id:" + id.toString(), null, null);
        } else {
            BizOrder wOptLock = BizOrder.getWOptLockForApp(id, repo2);
            if (!wOptLock.getVersion().equals(((AppUpdateBizOrderCommand) command).getVersion()))
                throw new VersionMismatchException();
            saveChangeRecord(command, changeId, OperationType.PUT, "id:" + id.toString(), null, wOptLock);
            BizOrder after = replaceEntity(wOptLock, command);
            repo.save(after);
        }
    }

    @Transactional
    public CreatedEntityRep create(AppCreateBizOrderCommand command, String changeId) {
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            return new CreatedEntityRep();
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
            String entityType = getEntityName();
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType, null, "sc:1");
            CreatedEntityRep createdEntityRep = new CreatedEntityRep();
            long l = Long.parseLong(appChangeRecordCardRepSumPagedRep.getData().get(0).getQuery().replace("id:", ""));
            createdEntityRep.setId(l);
            return createdEntityRep;
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            saveChangeRecord(command, changeId, OperationType.POST, "id:", null, null);
            return new CreatedEntityRep();
        } else {
            saveChangeRecord(command, changeId, OperationType.POST, "id:" + command.getOrderId(), null, null);
            BizOrder created = createEntity(command.getOrderId(), command);
            BizOrder save = repo.save(created);
            return new CreatedEntityRep(save);
        }
    }

    public void validate(AppValidateBizOrderCommand command) {
        AppProductSumPagedRep productsInfo = productService.getProductsInfo(command.getProductList());
        if (!BizOrder.validateProducts(productsInfo, command.getProductList())) {
            throw new ProductInfoValidationException();
        }
    }

    @Override
    public BizOrder replaceEntity(BizOrder bizOrder, Object command) {
        return bizOrder.replace((AppUpdateBizOrderCommand) command);
    }

    @Override
    public Void getEntitySumRepresentation(BizOrder bizOrder) {
        return null;
    }

    @Override
    public AppBizOrderRep getEntityRepresentation(BizOrder bizOrder) {
        return new AppBizOrderRep(bizOrder);
    }

    @Override
    protected BizOrder createEntity(long id, Object command) {
        return BizOrder.create((AppCreateBizOrderCommand) command);
    }

    @Override
    public void preDelete(BizOrder bizOrder) {

    }

    @Override
    public void postDelete(BizOrder bizOrder) {

    }

    @Override
    protected void prePatch(BizOrder bizOrder, Map<String, Object> params, VoidTypedClass middleLayer) {

    }

    @Override
    protected void postPatch(BizOrder bizOrder, Map<String, Object> params, VoidTypedClass middleLayer) {

    }

}
