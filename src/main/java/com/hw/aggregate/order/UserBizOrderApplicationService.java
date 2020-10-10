package com.hw.aggregate.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.command.UserCreateBizOrderCommand;
import com.hw.aggregate.order.command.UserUpdateBizOrderAddressCommand;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderQueryRegistry;
import com.hw.aggregate.order.representation.BizOrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.BizOrderPaymentLinkRepresentation;
import com.hw.aggregate.order.representation.UserBizOrderCardRep;
import com.hw.aggregate.order.representation.UserBizOrderRep;
import com.hw.shared.IdGenerator;
import com.hw.shared.UserThreadLocal;
import com.hw.shared.idempotent.AppChangeRecordApplicationService;
import com.hw.shared.idempotent.OperationType;
import com.hw.shared.rest.DefaultRoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
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
    private ProductService productService;

    @Autowired
    private ObjectMapper om2;

    @Autowired
    private SagaOrchestratorService sagaOrchestratorService;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private EntityManager entityManager;

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

    @Transactional
    public void replaceById(Long id, Object command, String changeId) {
        BizOrder wOptLock = BizOrder.getWOptLockForUser(id, UserThreadLocal.get(), repo2);
        saveChangeRecord(command, changeId, OperationType.PUT, "id:" + id.toString(),null, wOptLock);
        BizOrder after = replaceEntity(wOptLock, command);
        repo.save(after);
    }

    public BizOrderPaymentLinkRepresentation prepareOrder(Object command, String changeId) {
        long id = idGenerator.getId();
        BizOrder.prepare(id, (UserCreateBizOrderCommand) command, sagaOrchestratorService, changeId);
        UserBizOrderRep userBizOrderRep = readById(id);
        return new BizOrderPaymentLinkRepresentation(userBizOrderRep.getPaymentLink());
    }

    public BizOrderConfirmStatusRepresentation confirmPayment(Long id, String changeId) {
        UserBizOrderRep before = readById(id);
        BizOrder.confirmPayment(sagaOrchestratorService, changeId, before);
        Session unwrap = entityManager.unwrap(Session.class);
        try (Session session = unwrap.getSessionFactory().openSession()) {// auto close resource after return
            BizOrder load = session.load(BizOrder.class, id);
            return new BizOrderConfirmStatusRepresentation(load.isPaid());
        }
    }

    public BizOrderPaymentLinkRepresentation reserve(Long id, String changeId) {
        UserBizOrderRep before = readById(id);
        BizOrder.reserve(sagaOrchestratorService, changeId, before);
        Session unwrap = entityManager.unwrap(Session.class);
        try (Session session = unwrap.getSessionFactory().openSession()) {// auto close resource after return
            BizOrder load = session.load(BizOrder.class, id);
            return new BizOrderPaymentLinkRepresentation(load.getPaymentLink());
        }
    }

    @Override
    public BizOrder replaceEntity(BizOrder bizOrder, Object command) {
        bizOrder.updateAddress((UserUpdateBizOrderAddressCommand) command);
        return bizOrder;
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
