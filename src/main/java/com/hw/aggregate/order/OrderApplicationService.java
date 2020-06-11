package com.hw.aggregate.order;

import com.hw.aggregate.order.command.CreateOrderCommand;
import com.hw.aggregate.order.command.PlaceOrderAgainCommand;
import com.hw.aggregate.order.exception.OrderSchedulerProductRecycleException;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.OrderEvent;
import com.hw.aggregate.order.model.OrderState;
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

import static com.hw.shared.AppConstant.ORDER_DETAIL;

@Service
@Slf4j
@EnableScheduling
public class OrderApplicationService {

    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Value("${order.expireAfter}")
    private Long expireAfter;

    @Value("${order.draftExpireAfter}")
    private Long draftExpireAfter;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private ProductService productStorageService;

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

    @Transactional(readOnly = true)
    public OrderSummaryAdminRepresentation getAllOrdersForAdmin() {
        return new OrderSummaryAdminRepresentation(customerOrderRepository.findAll());
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public OrderSummaryCustomerRepresentation getAllOrders(String userId, Long profileId) {
        return new OrderSummaryCustomerRepresentation(customerOrderRepository.findByProfileId(profileId));
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public OrderCustomerRepresentation getOrderForCustomer(String userId, Long profileId, Long orderId) {
        return new OrderCustomerRepresentation(CustomerOrder.get(profileId, orderId, customerOrderRepository));
    }

    @ProfileExistAndOwnerOnly
    public OrderPaymentLinkRepresentation createNew(String userId, Long profileId, Long orderId, CreateOrderCommand command) {
        log.debug("start of createNew {}", orderId);
        CustomerOrder customerOrder = CustomerOrder.create(orderId, profileId, command.getProductList(), command.getAddress(), command.getPaymentType(), command.getPaymentAmt());
        StateMachine<OrderState, OrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(ORDER_DETAIL, customerOrder);
        stateMachine.sendEvent(OrderEvent.PERSIST);
        stateMachine.sendEvent(OrderEvent.NEW_ORDER);
        return new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    public OrderConfirmStatusRepresentation confirmPayment(String userId, Long profileId, Long orderId) {
        log.debug("start of confirmPayment {}", orderId);
        CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, customerOrderRepository);
        StateMachine<OrderState, OrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(ORDER_DETAIL, customerOrder);
        stateMachine.sendEvent(OrderEvent.CONFIRM_PAYMENT);
        return new OrderConfirmStatusRepresentation(customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    public void confirmOrder(String userId, Long profileId, Long orderId) {
        log.debug("start of confirmOrder {}", orderId);
        CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, customerOrderRepository);
        StateMachine<OrderState, OrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(ORDER_DETAIL, customerOrder);
        stateMachine.sendEvent(OrderEvent.CONFIRM_ORDER);
    }

    @ProfileExistAndOwnerOnly
    public OrderPaymentLinkRepresentation reserveAgain(String userId, Long profileId, Long orderId, PlaceOrderAgainCommand command) {
        log.info("reserve order {} again", orderId);
        CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, customerOrderRepository);
        customerOrder.updateAddress(command);
        StateMachine<OrderState, OrderEvent> stateMachine = customStateMachineBuilder.buildMachine(customerOrder.getOrderState());
        stateMachine.getExtendedState().getVariables().put(ORDER_DETAIL, customerOrder);
        stateMachine.sendEvent(OrderEvent.RESERVE);
        return new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), customerOrder.getPaid());
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String userId, Long profileId, Long orderId) {
        CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, customerOrderRepository);
        customerOrderRepository.delete(customerOrder);
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.release}")
    public void releaseExpiredOrder() {
        new TransactionTemplate(transactionManager)
                .execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        String transactionId = TransactionIdGenerator.getId();
                        log.info("Expired order scheduler started, transactionId generated {}", transactionId);
                        Date from = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - expireAfter * 60 * 1000));
                        List<CustomerOrder> expiredOrderList = customerOrderRepository.findExpiredNotPaidReserved(from);
                        log.info("Expired order(s) found {}", expiredOrderList.stream().map(CustomerOrder::getId).collect(Collectors.toList()).toString());
                        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
                        expiredOrderList.forEach(expiredOrder -> {
                            Map<String, Integer> orderProductMap = expiredOrder.getProductSummary();
                            orderProductMap.forEach((key, value) -> stringIntegerHashMap.merge(key, value, Integer::sum));
                        });
                        try {
                            if (!stringIntegerHashMap.keySet().isEmpty()) {
                                log.info("Release product(s) in order(s) :: " + stringIntegerHashMap.toString());
                                productStorageService.increaseOrderStorage(stringIntegerHashMap, transactionId);
                                /** update order state*/
                                expiredOrderList.forEach(e -> {
                                    e.setOrderState(OrderState.NOT_PAID_RECYCLED);
                                });
                            }
                            log.info("Expired order(s) released");
                            customerOrderRepository.saveAll(expiredOrderList);
                            customerOrderRepository.flush();
                        } catch (Exception ex) {
                            log.error("Error during release storage, revoke last operation", ex);
                            CompletableFuture.runAsync(() ->
                                    productStorageService.rollbackTransaction(transactionId), customExecutor
                            );
                            throw new OrderSchedulerProductRecycleException();
                        }
                    }
                });
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.resubmit}")
    public void resubmitOrder() {
        log.debug("start of resubmitOrder");
        List<CustomerOrder> paidReserved = customerOrderRepository.findPaidReserved();
        log.info("Paid reserved order(s) found {}", paidReserved.stream().map(CustomerOrder::getId).collect(Collectors.toList()));
        if (!paidReserved.isEmpty()) {
            // submit one order for now
            paidReserved.forEach(order -> {
                try {
                    confirmOrder(null, order.getProfileId(), order.getId());
                    log.info("Resubmit order {} success", order.getId());
                } catch (Exception e) {
                    log.error("Resubmit order {} failed", order.getId(), e);
                }
            });
        }
    }

    /**
     * in case of
     */
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.draft}")
    public void cleanDraftOrder() {
        log.debug("start of cleanDraftOrder");
        Date from = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - draftExpireAfter * 60 * 1000));
        List<CustomerOrder> draftOrders = customerOrderRepository.findExpiredDraftOrders(from);
        log.info("Draft order(s) found {}", draftOrders.stream().map(CustomerOrder::getId).collect(Collectors.toList()));
        if (!draftOrders.isEmpty()) {
            // clean one order for now
            draftOrders.forEach(order -> {
                try {
                    cleanUpDraftOrder(order);
                    log.info("Clean draft order {} success", order.getId());
                } catch (Exception e) {
                    log.error("Clean draft order {} failed", order.getId(), e);
                }
            });
        }
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    private void cleanUpDraftOrder(CustomerOrder order) {
        String nextTransactionId = order.getNextTransactionId();
        CompletableFuture.runAsync(() ->
                paymentService.rollbackTransaction(nextTransactionId), customExecutor
        );
        CompletableFuture.runAsync(() ->
                productService.rollbackTransaction(nextTransactionId), customExecutor
        );
        order.setOrderState(OrderState.DRAFT_CLEAN);
        try {
            customerOrderRepository.saveAndFlush(order);
        } catch (Exception ex) {
            log.error("error during data persist", ex);
        }
    }

    public String getOrderId() {
        return String.valueOf(idGenerator.getId());
    }
}
