package com.hw.aggregate.order;

import com.hw.aggregate.order.command.AppCreateBizOrderCommand;
import com.hw.aggregate.order.command.AppUpdateBizOrderCommand;
import com.hw.aggregate.order.command.AppValidateBizOrderCommand;
import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.product.AppProductSumPagedRep;
import com.hw.aggregate.order.representation.AppBizOrderRep;
import com.hw.shared.idempotent.OperationType;
import com.hw.shared.idempotent.representation.AppChangeRecordCardRep;
import com.hw.shared.rest.CreatedAggregateRep;
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

    @Override
    protected Long getAggregateId(Object object) {
        return ((AppCreateBizOrderCommand) object).getOrderId();
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
