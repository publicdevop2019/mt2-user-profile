package com.hw.aggregate.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.command.CreateOrderCommand;
import com.hw.aggregate.order.command.PlaceOrderAgainCommand;
import com.hw.aggregate.order.exception.*;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderAddress;
import com.hw.aggregate.order.model.OrderEvent;
import com.hw.aggregate.order.model.OrderState;
import com.hw.aggregate.order.representation.*;
import com.hw.config.ProfileExistAndOwnerOnly;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.IdGenerator;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableScheduling
public class OrderApplicationService {

    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Value("${order.expireAfter}")
    private Long expireAfter;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productStorageService;

    @Autowired
    private MessengerService messengerService;

    @Autowired
    private CartApplicationService cartApplicationService;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    @Qualifier("CustomPool")
    private Executor customExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;


    @Transactional(readOnly = true)
    public OrderSummaryAdminRepresentation getAllOrdersForAdmin() {
        return new OrderSummaryAdminRepresentation(orderRepository.findAll());
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public OrderSummaryCustomerRepresentation getAllOrders(String authUserId, Long profileId) {
        return new OrderSummaryCustomerRepresentation(orderRepository.findByProfileId(profileId));
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public OrderCustomerRepresentation getOrderForCustomer(String authUserId, Long profileId, Long orderId) {
        return new OrderCustomerRepresentation(CustomerOrder.get(profileId, orderId, orderRepository));
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderPaymentLinkRepresentation createNew(String authUserId, Long profileId, CreateOrderCommand newOrder) {
        log.debug("start of createNew");
        CustomerOrder customerOrder = CustomerOrder.create(idGenerator.getId(), profileId, newOrder.getProductList(), newOrder.getAddress(), newOrder.getPaymentType(), newOrder.getPaymentAmt());

        log.debug("order with id {} generated", customerOrder.getId().toString());

        // validate order product info
        CompletableFuture<Void> validateResultFuture = CompletableFuture.runAsync(() ->
                productStorageService.validateProductInfo(customerOrder.getReadOnlyProductList()), customExecutor
        );

        // generate payment QR link
        String operationToken = getOperationToken();
        log.debug("optToken generated {}", operationToken);
        CompletableFuture<String> paymentQRLinkFuture = CompletableFuture.supplyAsync(() ->
                paymentService.generatePaymentLink(customerOrder.getId().toString()), customExecutor
        );

        // decrease order storage
        CompletableFuture<Void> decreaseOrderStorageFuture = CompletableFuture.runAsync(() ->
                productStorageService.decreaseOrderStorage(customerOrder.getProductSummary(), operationToken), customExecutor
        );
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(validateResultFuture, paymentQRLinkFuture, decreaseOrderStorageFuture);
        String paymentLink = null;
        try {
            allDoneFuture.get();
            paymentLink = paymentQRLinkFuture.get();
        } catch (ExecutionException e) {
            log.error("error during reserve order", e);
            CompletableFuture.runAsync(() ->
                    productStorageService.rollbackChange(operationToken), customExecutor
            );
            // if decreaseOrderStorageFuture got timeout, the order storage should get rollback
            if (decreaseOrderStorageFuture.isCompletedExceptionally())
                throw new OrderStorageDecreaseException();
            if (paymentQRLinkFuture.isCompletedExceptionally())
                throw new PaymentQRLinkGenerationException();
            if (validateResultFuture.isCompletedExceptionally())
                throw new ProductInfoValidationException();
            throw new OrderCreationUnknownException();
        } catch (InterruptedException e) {
            log.warn("thread was interrupted", e);
            CompletableFuture.runAsync(() ->
                    productStorageService.rollbackChange(operationToken), customExecutor
            );
            Thread.currentThread().interrupt();
        }
        customerOrder.setPaymentLink(paymentLink);
        log.debug("order storage decreased");
        cartApplicationService.clearCartItem(profileId);
        orderRepository.saveAndFlush(customerOrder);
        return new OrderPaymentLinkRepresentation(paymentLink, false);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderConfirmStatusRepresentation confirmPayment(String authUserId, Long profileId, Long orderId) {
        log.debug("start of confirmPayment");
        CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, orderRepository);
        Boolean paymentStatus = paymentService.confirmPaymentStatus(orderId.toString());
        if (Boolean.TRUE.equals(paymentStatus)) {
            OrderEvent.CONFIRM_PAYMENT.nextState(customerOrder);
            orderRepository.saveAndFlush(customerOrder);
        }
        OrderConfirmStatusRepresentation confirmStatusRepresentation = new OrderConfirmStatusRepresentation();
        confirmStatusRepresentation.setPaymentStatus(paymentStatus);
        return confirmStatusRepresentation;
    }

    @ProfileExistAndOwnerOnly
    public void confirmOrder(String authUserId, Long profileId, Long orderId) {
        log.debug("start of confirmOrder");
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, orderRepository);
                String operationToken = getOperationToken();
                CompletableFuture<Void> decreaseActualStorageFuture = CompletableFuture.runAsync(() ->
                        productStorageService.decreaseActualStorage(customerOrder.getProductSummary(), operationToken), customExecutor
                );
                try {
                    decreaseActualStorageFuture.get();
                    OrderEvent.DECREASE_ACTUAL_STORAGE.nextState(customerOrder);
                    entityManager.persist(customerOrder);
                } catch (Exception e) {
                    log.error("error during confirm order, rollback last change", e);
                    productStorageService.rollbackChange(operationToken);
                    throw new ActualStorageDecreaseException();
                }
            }
        });
        messengerService.notifyBusinessOwner(new HashMap<>());
    }

    /**
     * only address and payment_method can be updated
     */
    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderPaymentLinkRepresentation placeAgain(String authUserId, Long profileId, Long orderId, PlaceOrderAgainCommand placeOrderAgainCommand) {
        log.info("place order {} again", orderId);
        CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, orderRepository);
        OrderPaymentLinkRepresentation representation;
        updateAddressIfApplicable(placeOrderAgainCommand, customerOrder);
        if (customerOrder.getOrderState().equals(OrderState.PAID_RECYCLED)) {
            representation = new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), true);
        } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RECYCLED)) {
            representation = new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), false);
        } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RESERVED)) {
            orderRepository.saveAndFlush(customerOrder);
            return new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), false);
        } else {
            throw new StateChangeException();
        }
        // decrease order storage
        String operationToken = getOperationToken();
        CompletableFuture<Void> decreaseOrderStorageFuture = CompletableFuture.runAsync(() ->
                productStorageService.decreaseOrderStorage(customerOrder.getProductSummary(), operationToken), customExecutor
        );
        customerOrder.updateModifiedByUserAt();
        try {
            decreaseOrderStorageFuture.get();
            // if db throws ex then revoke is required
            OrderEvent.RESERVE.nextState(customerOrder);
            orderRepository.saveAndFlush(customerOrder);
        } catch (Exception e) {
            log.error("error during place order again, rollback last operation", e);
            CompletableFuture.runAsync(() ->
                    productStorageService.rollbackChange(operationToken), customExecutor
            );
            if (decreaseOrderStorageFuture.isCompletedExceptionally()) {
                throw new OrderStorageDecreaseException();
            }
            throw new OrderCreationUnknownException();
        }
        return representation;
    }

    private void updateAddressIfApplicable(PlaceOrderAgainCommand placeOrderAgainCommand, CustomerOrder customerOrder) {
        if (placeOrderAgainCommand.getAddress() != null) {
            CustomerOrderAddress customerOrderAddress = new CustomerOrderAddress();
            customerOrderAddress.setOrderAddressCity(placeOrderAgainCommand.getAddress().getCity());
            customerOrderAddress.setOrderAddressCountry(placeOrderAgainCommand.getAddress().getCountry());
            customerOrderAddress.setOrderAddressFullName(placeOrderAgainCommand.getAddress().getFullName());
            customerOrderAddress.setOrderAddressLine1(placeOrderAgainCommand.getAddress().getLine1());
            customerOrderAddress.setOrderAddressLine2(placeOrderAgainCommand.getAddress().getLine2());
            customerOrderAddress.setOrderAddressPhoneNumber(placeOrderAgainCommand.getAddress().getPhoneNumber());
            customerOrderAddress.setOrderAddressProvince(placeOrderAgainCommand.getAddress().getProvince());
            customerOrderAddress.setOrderAddressPostalCode(placeOrderAgainCommand.getAddress().getPostalCode());
            customerOrder.setAddress(customerOrderAddress);
        }
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String authUserId, Long profileId, Long orderId) {
        CustomerOrder customerOrder = CustomerOrder.getForUpdate(profileId, orderId, orderRepository);
        orderRepository.delete(customerOrder);
    }

    @Transactional
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.release}")
    public void releaseExpiredOrder() {
        String changeId = getOperationToken();
        log.info("Expired order scheduler started, optToken generated {}", changeId);
        long l = Instant.now().toEpochMilli() - expireAfter * 60 * 1000;
        Instant instant = Instant.ofEpochMilli(l);
        Date from = Date.from(instant);
        List<CustomerOrder> expiredOrderList = orderRepository.findExpiredNotPaidReserved(from);
        List<Long> collect = expiredOrderList.stream().map(CustomerOrder::getId).collect(Collectors.toList());
        log.info("Expired order(s) found {}", collect.toString());
        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
        expiredOrderList.forEach(expiredOrder -> {
            Map<String, Integer> orderProductMap = expiredOrder.getProductSummary();
            orderProductMap.forEach((key, value) -> stringIntegerHashMap.merge(key, value, Integer::sum));
        });
        try {
            if (!stringIntegerHashMap.keySet().isEmpty()) {
                log.info("Release product(s) in order(s) :: " + stringIntegerHashMap.toString());
                productStorageService.increaseOrderStorage(stringIntegerHashMap, changeId);
                /** update order state*/
                expiredOrderList.forEach(OrderEvent.RECYCLE_ORDER_STORAGE::nextState);
            }
            log.info("Expired order(s) released");
            orderRepository.saveAll(expiredOrderList);
            orderRepository.flush();
        } catch (Exception ex) {
            log.error("Error during release storage, revoke last operation", ex);
            CompletableFuture.runAsync(() ->
                    productStorageService.rollbackChange(changeId)
            );
            throw new OrderSchedulerProductRecycleException();
        }
    }

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.resubmit}")
    public void resubmitOrder() {
        log.debug("start of resubmitOrder");
        List<CustomerOrder> paidReserved = orderRepository.findPaidReserved();
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

    private String getOperationToken() {
        return UUID.randomUUID().toString();
    }
}
