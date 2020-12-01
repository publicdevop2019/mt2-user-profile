package com.hw.aggregate.order;

import com.hw.aggregate.order.command.UserCreateBizOrderCommand;
import com.hw.aggregate.order.command.UserUpdateBizOrderAddressCommand;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.representation.BizOrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.BizOrderPaymentLinkRepresentation;
import com.hw.aggregate.order.representation.UserBizOrderCardRep;
import com.hw.aggregate.order.representation.UserBizOrderRep;
import com.hw.shared.rest.RoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
@Slf4j
public class UserBizOrderApplicationService extends RoleBasedRestfulService<BizOrder, UserBizOrderCardRep, UserBizOrderRep, VoidTypedClass> {
    {
        entityClass = BizOrder.class;
        role = RestfulQueryRegistry.RoleEnum.USER;
    }

    @Autowired
    SagaOrchestratorService sagaOrchestratorService;
    @Autowired
    private EntityManager entityManager;

    public BizOrderPaymentLinkRepresentation prepareOrder(Object command, String changeId) {
        long id = idGenerator.getId();
        BizOrder.prepare(id, (UserCreateBizOrderCommand) command, sagaOrchestratorService, changeId);
        UserBizOrderRep userBizOrderRep = readById(id);
        return new BizOrderPaymentLinkRepresentation(userBizOrderRep.getPaymentLink());
    }

    /**
     * open new session to by pass hibernate cache, so it can read latest value
     * @param id
     * @param changeId
     * @return
     */
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

}
