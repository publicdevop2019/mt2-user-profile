package com.hw.aggregate.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.exception.*;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.OrderState;
import com.hw.aggregate.order.representation.*;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.IdGenerator;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
        List<CustomerOrder> byProfileId = orderRepository.findByProfileId(profileId);
        return new OrderSummaryCustomerRepresentation(byProfileId);
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public OrderCustomerRepresentation getOrderForCustomer(String authUserId, Long profileId, Long orderId) {
        return new OrderCustomerRepresentation(getOrderForCustomerReadOnly(profileId, orderId));
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderPaymentLinkRepresentation createNew(String authUserId, Long profileId, CreateOrderCommand newOrder) {
        log.debug("start of createNew");
        CustomerOrder customerOrder = CustomerOrder.create(idGenerator.getId(), profileId, newOrder.getProductList(), newOrder.getAddress(), newOrder.getPaymentType(), newOrder.getPaymentAmt());

        // validate order product info
        CompletableFuture<Void> validateResultFuture = CompletableFuture.runAsync(() ->
                productStorageService.validateProductInfo(customerOrder.getReadOnlyProductList()), customExecutor
        );

        log.debug("order with id {} generated", customerOrder.getId().toString());

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
            if (paymentQRLinkFuture.isCompletedExceptionally() && !decreaseOrderStorageFuture.isCompletedExceptionally())
                throw new PaymentQRLinkGenerationException();
            if (validateResultFuture.isCompletedExceptionally() && !decreaseOrderStorageFuture.isCompletedExceptionally())
                throw new ProductInfoValidationException();
            Thread.currentThread().interrupt();
            throw new OrderCreationUnknownException();
        } catch (InterruptedException e) {
            log.warn("thread was interrupted", e);
            Thread.currentThread().interrupt();
        }
        customerOrder.setPaymentLink(paymentLink);
        log.debug("order storage decreased");
        cartApplicationService.clearCartItem(profileId);
        orderRepository.save(customerOrder);
        return new OrderPaymentLinkRepresentation(paymentLink, Boolean.FALSE);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderConfirmStatusRepresentation confirmPayment(String authUserId, Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        log.debug("start of confirmPayment");
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, confirmOrderPaymentCommand.getOrderId());
        Boolean paymentStatus = paymentService.confirmPaymentStatus(confirmOrderPaymentCommand.getOrderId().toString());
        if (Boolean.TRUE.equals(paymentStatus)) {
            if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RESERVED)) {
                customerOrder.toPaidReserved();
            } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RECYCLED)) {
                customerOrder.toPaidRecycled();
            } else {
                throw new StateChangeException();
            }
            orderRepository.saveAndFlush(customerOrder);
        }
        OrderConfirmStatusRepresentation confirmStatusRepresentation = new OrderConfirmStatusRepresentation();
        confirmStatusRepresentation.put("paymentStatus", paymentStatus);
        return confirmStatusRepresentation;
    }

    @ProfileExistAndOwnerOnly
    public void confirmOrder(String authUserId, Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        log.debug("start of confirmOrder");
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, confirmOrderPaymentCommand.getOrderId());
                customerOrder.toConfirmed();
                String operationToken = getOperationToken();
                CompletableFuture<Void> decreaseActualStorageFuture = CompletableFuture.runAsync(() ->
                        productStorageService.decreaseActualStorage(customerOrder.getProductSummary(), operationToken), customExecutor
                );
                try {
                    decreaseActualStorageFuture.get();
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

    @Transactional
    public void updateOrderAdmin(Long orderId, UpdateOrderAdminCommand newOrder) {
        newOrder.setId(orderId);
        Optional<CustomerOrder> byId = orderRepository.findById(orderId);
        if (byId.isEmpty())
            throw new OrderNotExistException();
        BeanUtils.copyProperties(newOrder, byId.get());
        orderRepository.save(byId.get());
    }

    /**
     * only address and payment_method can be updated
     */
    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderPaymentLinkRepresentation placeAgain(String authUserId, Long profileId, Long orderId, PlaceOrderAgainCommand placeOrderAgainCommand) {
        log.info("place order {} again", orderId);
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, orderId);
        OrderPaymentLinkRepresentation representation;
        if (customerOrder.getOrderState().equals(OrderState.PAID_RECYCLED)) {
            representation = paidRecycledLogic(placeOrderAgainCommand, customerOrder);
        } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RECYCLED)) {
            representation = notPaidRecycledLogic(placeOrderAgainCommand, customerOrder);
        } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RESERVED)) {
            return notPaidReservedLogic(placeOrderAgainCommand, customerOrder);
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

    private OrderPaymentLinkRepresentation notPaidReservedLogic(PlaceOrderAgainCommand placeOrderAgainCommand, CustomerOrder customerOrder) {
        if (placeOrderAgainCommand != null && (placeOrderAgainCommand.getPaymentType() != null || placeOrderAgainCommand.getAddress() != null)) {
            log.info("updating order address & paymentType if applicable");
            if (placeOrderAgainCommand.getAddress() != null)
                customerOrder.setAddress(placeOrderAgainCommand.getAddress());
        }
        orderRepository.saveAndFlush(customerOrder);
        return new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), Boolean.FALSE);
    }

    private OrderPaymentLinkRepresentation notPaidRecycledLogic(PlaceOrderAgainCommand placeOrderAgainCommand, CustomerOrder customerOrder) {
        OrderPaymentLinkRepresentation representation;
        customerOrder.toNotPaidReserved();
        if (placeOrderAgainCommand != null && (placeOrderAgainCommand.getPaymentType() != null || placeOrderAgainCommand.getAddress() != null)) {
            log.info("updating order address & paymentType if applicable");
            if (placeOrderAgainCommand.getAddress() != null)
                customerOrder.setAddress(placeOrderAgainCommand.getAddress());
        }
        representation = new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), Boolean.FALSE);
        return representation;
    }

    private OrderPaymentLinkRepresentation paidRecycledLogic(PlaceOrderAgainCommand placeOrderAgainCommand, CustomerOrder customerOrder) {
        OrderPaymentLinkRepresentation representation;
        customerOrder.toPaidReserved();
        if (placeOrderAgainCommand != null && placeOrderAgainCommand.getAddress() != null) {
            log.info("updating order address");
            customerOrder.setAddress(placeOrderAgainCommand.getAddress());
        }
        representation = new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), Boolean.TRUE);
        return representation;
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String authUserId, Long profileId, DeleteOrderCustomerCommand deleteOrderCustomerCommand) {
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, deleteOrderCustomerCommand.getOrderId());
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
                expiredOrderList.forEach(CustomerOrder::toNotPaidRecycled);
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
                    confirmOrder(null, order.getProfileId(), new ConfirmOrderPaymentCommand(order.getId()));
                    log.info("Resubmit order {} success", order.getId());
                } catch (Exception e) {
                    log.error("Resubmit order {} failed", order.getId(), e);
                }
            });
        }
    }

    private CustomerOrder getOrderForCustomerToUpdate(Long profileId, Long orderId) {
        Optional<CustomerOrder> byId = orderRepository.findByIdForUpdate(orderId);
        if (byId.isEmpty())
            throw new OrderNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new OrderAccessException();
        return byId.get();
    }

    private CustomerOrder getOrderForCustomerReadOnly(Long profileId, Long orderId) {
        Optional<CustomerOrder> byId = orderRepository.findById(orderId);
        if (byId.isEmpty())
            throw new OrderNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new OrderAccessException();
        return byId.get();
    }

    private String getOperationToken() {
        return UUID.randomUUID().toString();
    }
}
