package com.hw.aggregate.order;

import com.hw.aggregate.order.command.CreateBizOrderCommand;
import com.hw.aggregate.order.command.PlaceBizOrderAgainCommand;
import com.hw.aggregate.order.exception.BizOrderSchedulerProductRecycleException;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderEvent;
import com.hw.aggregate.order.model.BizOrderStatus;
import com.hw.aggregate.order.representation.*;
import com.hw.config.CustomStateMachineBuilder;
import com.hw.config.ProfileExistAndOwnerOnly;
import com.hw.config.TransactionIdGenerator;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.IdGenerator;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.hw.aggregate.order.model.AppConstant.BIZ_ORDER;
import static com.hw.aggregate.order.model.AppConstant.UPDATE_ADDRESS_CMD;
import static com.hw.config.CustomStateMachineEventListener.ERROR_CLASS;

@Service
@Slf4j
@EnableScheduling
public class BizOrderApplicationService {

    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Value("${order.expireAfter}")
    private Long expireAfter;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    private BizOrderRepository customerOrderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    @Qualifier("CustomPool")
    private TaskExecutor customExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomStateMachineBuilder customStateMachineBuilder;

    @Autowired
    private PaymentService paymentService;

    @Transactional(readOnly = true)
    public BizOrderSummaryAdminRepresentation getAllOrdersForAdmin() {
        log.info("start of getAllOrdersForAdmin");
        return new BizOrderSummaryAdminRepresentation(customerOrderRepository.findAll());
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public BizOrderSummaryCustomerRepresentation getAllOrders(String userId, Long profileId) {
        log.info("start of getAllOrders");
        return new BizOrderSummaryCustomerRepresentation(customerOrderRepository.findByProfileId(profileId));
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public BizOrderCustomerRepresentation getOrderForCustomer(String userId, Long profileId, Long orderId) {
        log.info("start of getOrderForCustomer");
        return new BizOrderCustomerRepresentation(BizOrder.get(profileId, orderId, customerOrderRepository));
    }

    @ProfileExistAndOwnerOnly
    public BizOrderPaymentLinkRepresentation createNew(String userId, Long profileId, Long orderId, CreateBizOrderCommand command) {
        log.debug("start of createNew {}", orderId);
        BizOrder customerOrder = BizOrder.create(orderId, profileId, command.getProductList(), command.getAddress(), command.getPaymentType(), command.getPaymentAmt());
        StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
        stateMachine.sendEvent(BizOrderEvent.PREPARE_NEW_ORDER);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        stateMachine.sendEvent(BizOrderEvent.NEW_ORDER);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        return new BizOrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    public BizOrderConfirmStatusRepresentation confirmPayment(String userId, Long profileId, Long orderId) {
        log.debug("start of confirmPayment {}", orderId);
        BizOrder customerOrder = BizOrder.get(profileId, orderId, customerOrderRepository);
        StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
        stateMachine.sendEvent(BizOrderEvent.CONFIRM_PAYMENT);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        return new BizOrderConfirmStatusRepresentation(customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    public void confirmOrder(String userId, Long profileId, Long orderId) {
        log.debug("start of confirmOrder {}", orderId);
        BizOrder customerOrder = BizOrder.get(profileId, orderId, customerOrderRepository);
        StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
        stateMachine.sendEvent(BizOrderEvent.PREPARE_CONFIRM_ORDER);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        stateMachine.sendEvent(BizOrderEvent.CONFIRM_ORDER);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
    }

    @ProfileExistAndOwnerOnly
    public BizOrderPaymentLinkRepresentation reserveAgain(String userId, Long profileId, Long orderId, PlaceBizOrderAgainCommand command) {
        log.info("reserve order {} again", orderId);
        BizOrder customerOrder = BizOrder.get(profileId, orderId, customerOrderRepository);
        StateMachine<BizOrderStatus, BizOrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(UPDATE_ADDRESS_CMD, command);
        stateMachine.getExtendedState().getVariables().put(BIZ_ORDER, customerOrder);
        stateMachine.sendEvent(BizOrderEvent.PREPARE_RESERVE);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        stateMachine.sendEvent(BizOrderEvent.RESERVE);
        if (stateMachine.hasStateMachineError()) {
            throw stateMachine.getExtendedState().get(ERROR_CLASS, RuntimeException.class);
        }
        return new BizOrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String userId, Long profileId, Long orderId) {
        BizOrder customerOrder = BizOrder.getWOptLock(profileId, orderId, customerOrderRepository);
        customerOrderRepository.delete(customerOrder);
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.release}")
    public void releaseExpiredOrder() {
        new TransactionTemplate(transactionManager)
                .execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        String transactionId = TransactionIdGenerator.getTxId();
                        Date from = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - expireAfter * 60 * 1000));
                        List<BizOrder> expiredOrderList = customerOrderRepository.findExpiredNotPaidReserved(from);
                        log.info("expired order(s) found {}", expiredOrderList.stream().map(BizOrder::getId).collect(Collectors.toList()).toString());
                        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
                        expiredOrderList.forEach(expiredOrder -> {
                            Map<String, Integer> orderProductMap = expiredOrder.getProductSummary();
                            orderProductMap.forEach((key, value) -> stringIntegerHashMap.merge(key, value, Integer::sum));
                        });
                        try {
                            if (!stringIntegerHashMap.keySet().isEmpty()) {
                                log.info("release product(s) in order(s) :: " + stringIntegerHashMap.toString());
                                productService.increaseOrderStorage(stringIntegerHashMap, transactionId);
                                /** update order state*/
                                expiredOrderList.forEach(e -> {
                                    e.setOrderState(BizOrderStatus.NOT_PAID_RECYCLED);
                                });
                            }
                            log.info("expired order(s) released");
                            customerOrderRepository.saveAll(expiredOrderList);
                            customerOrderRepository.flush();
                        } catch (Exception ex) {
                            log.error("error during release storage, revoke last operation", ex);
                            CompletableFuture.runAsync(() ->
                                    productService.rollbackTransaction(transactionId), customExecutor
                            );
                            throw new BizOrderSchedulerProductRecycleException();
                        }
                    }
                });
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.resubmit}")
    public void resubmitOrder() {
        List<BizOrder> paidReserved = customerOrderRepository.findPaidReserved();
        log.info("paid reserved order(s) found {}", paidReserved.stream().map(BizOrder::getId).collect(Collectors.toList()));
        if (!paidReserved.isEmpty()) {
            // submit one order for now
            paidReserved.forEach(order -> {
                try {
                    confirmOrder(null, order.getProfileId(), order.getId());
                    log.info("resubmit order {} success", order.getId());
                } catch (Exception e) {
                    log.error("resubmit order {} failed", order.getId(), e);
                }
            });
        }
    }

    //@todo create scheduler for started task
    public String getOrderId() {
        return String.valueOf(idGenerator.getId());
    }
}
