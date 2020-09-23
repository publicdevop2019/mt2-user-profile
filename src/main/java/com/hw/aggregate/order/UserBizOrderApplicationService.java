package com.hw.aggregate.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.command.UserCreateBizOrderCommand;
import com.hw.aggregate.order.command.UserPlaceBizOrderAgainCommand;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderQueryRegistry;
import com.hw.aggregate.order.representation.BizOrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.BizOrderPaymentLinkRepresentation;
import com.hw.aggregate.order.representation.UserBizOrderCardRep;
import com.hw.aggregate.order.representation.UserBizOrderRep;
import com.hw.config.CustomStateMachineBuilder;
import com.hw.shared.IdGenerator;
import com.hw.shared.idempotent.AppChangeRecordApplicationService;
import com.hw.shared.idempotent.OperationType;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Slf4j
public class UserBizOrderApplicationService extends DefaultRoleBasedRestfulService<BizOrder, UserBizOrderCardRep, UserBizOrderRep, VoidTypedClass> {
    @Autowired
    private BizOrderRepository repo2;
    @Autowired
    private AppChangeRecordApplicationService changeHistoryRepository;

    @Autowired
    private IdGenerator idGenerator2;

    @Autowired
    private BizOrderQueryRegistry skuQueryRegistry;

    @Autowired
    private ObjectMapper om2;

    @Autowired
    private CustomStateMachineBuilder customStateMachineBuilder;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @PostConstruct
    private void setUp() {
        repo = repo2;
        idGenerator = idGenerator2;
        queryRegistry = skuQueryRegistry;
        entityClass = BizOrder.class;
        role = RestfulQueryRegistry.RoleEnum.USER;
        om = om2;
        appChangeRecordApplicationService = changeHistoryRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BizOrderPaymentLinkRepresentation createNew(Object command, String changeId) {
        long id = idGenerator.getId();
        saveChangeRecord(null, changeId, OperationType.POST, "id:" + id);
        BizOrder bizOrder = BizOrder.create(id, (UserCreateBizOrderCommand) command, customStateMachineBuilder);
        BizOrder save = repo.save(bizOrder);
        return new BizOrderPaymentLinkRepresentation(save.getPaymentLink(), save.getPaid());
    }

    @Transactional
    public BizOrderConfirmStatusRepresentation confirmPayment(Long id, String userId) {
        BizOrder customerOrder = BizOrder.getWOptLock(id, userId, repo2);
        customerOrder.confirmPayment(customStateMachineBuilder);
        return new BizOrderConfirmStatusRepresentation(customerOrder.getPaid());
    }

    public void submitOrder(Long id, String userId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);// read just committed task
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                BizOrder customerOrder = BizOrder.getWOptLock(id, userId, repo2);
                customerOrder.submit(customStateMachineBuilder);
            }
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BizOrderPaymentLinkRepresentation reserve(Long id, String userId, UserPlaceBizOrderAgainCommand command) {
        BizOrder customerOrder = BizOrder.getWOptLock(id, userId, repo2);
        customerOrder.reserve(customStateMachineBuilder, command);
        return new BizOrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), customerOrder.getPaid());
    }

    @Override
    public BizOrder replaceEntity(BizOrder bizOrder, Object command) {
        return null;
    }

    @Override
    public UserBizOrderCardRep getEntitySumRepresentation(BizOrder bizOrder) {
        return new UserBizOrderCardRep(bizOrder);
    }

    @Override
    public UserBizOrderRep getEntityRepresentation(BizOrder bizOrder) {
        return new UserBizOrderRep(bizOrder);
    }

    @Override
    protected BizOrder createEntity(long id, Object command) {
        return null;
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