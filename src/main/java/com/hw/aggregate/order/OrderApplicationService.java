package com.hw.aggregate.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.exception.*;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderPaymentStatus;
import com.hw.aggregate.order.representation.*;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Qualifier("CustomPool")
    private Executor customExecutor;

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
    public OrderPaymentLinkRepresentation reserveOrder(String authUserId, Long profileId, CreateOrderCommand newOrder) {
        log.debug("start of reserve order");
        CustomerOrder customerOrder = CustomerOrder.create(profileId, newOrder.getProductList(), newOrder.getAddress(), newOrder.getPaymentType(), newOrder.getPaymentAmt());

        // validate order
        CompletableFuture<Void> validateResultFuture = CompletableFuture.runAsync(() ->
                productStorageService.validateProductInfo(customerOrder.getReadOnlyProductList()), customExecutor
        );
        customerOrder.validatePaymentAmount();

        // generate order id, use db generate orderId
        CustomerOrder save = orderRepository.save(customerOrder);
        String reservedOrderId = save.getId().toString();
        log.debug("order id generated & saved");

        // generate payment QR link
        String operationToken = getOperationToken();
        CompletableFuture<String> paymentQRLinkFuture = CompletableFuture.supplyAsync(() ->
                paymentService.generatePaymentLink(reservedOrderId), customExecutor
        );

        // decrease order storage
        CompletableFuture<Void> decreaseOrderStorageFuture = CompletableFuture.runAsync(() ->
                productStorageService.decreaseOrderStorage(customerOrder.getProductSummary(), operationToken), customExecutor
        );
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(validateResultFuture, paymentQRLinkFuture, decreaseOrderStorageFuture);
        String paymentLink;
        try {
            allDoneFuture.get();
            paymentLink = paymentQRLinkFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("error during reserve order", e);
            CompletableFuture.runAsync(() ->
                    productStorageService.revokeOrderStorageChange(operationToken), customExecutor
            );
            // if decreaseOrderStorageFuture got timeout, the order storage should get revoked
            if (decreaseOrderStorageFuture.isCompletedExceptionally())
                throw new OrderStorageDecreaseException();
            if (paymentQRLinkFuture.isCompletedExceptionally() && !decreaseOrderStorageFuture.isCompletedExceptionally())
                throw new PaymentQRLinkGenerationException();
            if (validateResultFuture.isCompletedExceptionally() && !decreaseOrderStorageFuture.isCompletedExceptionally())
                throw new ProductInfoValidationException();
            throw new OrderCreationUnknownException();
        }
        log.debug("order storage decreased");
        cartApplicationService.clearCartItem(profileId);
        return new OrderPaymentLinkRepresentation(paymentLink);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderConfirmStatusRepresentation confirmOrderPaymentStatus(String authUserId, Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        OrderConfirmStatusRepresentation confirmStatusRepresentation = new OrderConfirmStatusRepresentation();
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, confirmOrderPaymentCommand.orderId);

        if (customerOrder.getPaymentStatus().equals(CustomerOrderPaymentStatus.paid)) {
            confirmStatusRepresentation.put("paymentStatus", Boolean.TRUE);
            return confirmStatusRepresentation;
        }

        Boolean paymentStatus = paymentService.confirmPaymentStatus(confirmOrderPaymentCommand.orderId.toString());
        CompletableFuture<Void> decreaseActualStorageFuture = null;
        if (paymentStatus) {
            log.debug("notify business owner asynchronously");
            messengerService.notifyBusinessOwner(new HashMap<>());
            decreaseActualStorageFuture = CompletableFuture.runAsync(() ->
                    productStorageService.decreaseActualStorage(customerOrder.getProductSummary(), customerOrder.getId().toString()), customExecutor
            );
        }
        customerOrder.setPaymentStatus(paymentStatus);
        orderRepository.save(customerOrder);
        if (paymentStatus) {
            try {
                decreaseActualStorageFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("error during confirm order", e);
                if (decreaseActualStorageFuture.isCompletedExceptionally())
                    // actual storage will not revoked
                    // when user confirm again, decreaseActualStorage api is idempotent with same orderId
                    throw new ActualStorageDecreaseException();
            }
        }
        confirmStatusRepresentation.put("paymentStatus", paymentStatus);
        return confirmStatusRepresentation;
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
    public OrderPaymentLinkRepresentation placeOrderAgain(String authUserId, Long profileId, Long orderId, PlaceOrderAgainCommand placeOrderAgainCommand) {
        log.info("place order {} again", orderId);
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, orderId);
        // if order already paid, just return paymentLink
        //@todo add paymentLink field in order entity
        if (customerOrder.getPaymentStatus().equals(CustomerOrderPaymentStatus.paid)) {
            // generate payment QR link
            CompletableFuture<String> paymentQRLinkFuture = CompletableFuture.supplyAsync(() ->
                    paymentService.generatePaymentLink(String.valueOf(orderId)), customExecutor
            );
            try {
                String paymentLink = paymentQRLinkFuture.get();
                return new OrderPaymentLinkRepresentation(paymentLink);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        log.info("updating order address & paymentType if applicable");
        customerOrder.setAddress(placeOrderAgainCommand.getAddress());
        customerOrder.setPaymentType(placeOrderAgainCommand.getPaymentType());


        // generate payment QR link
        CompletableFuture<String> paymentQRLinkFuture = CompletableFuture.supplyAsync(() ->
                paymentService.generatePaymentLink(String.valueOf(orderId)), customExecutor
        );
        // decrease order storage
        CompletableFuture<Void> decreaseOrderStorageFuture = null;
        CompletableFuture<Void> allDoneFuture;
        boolean decreaseCallRequired = customerOrder.getExpired() && customerOrder.getRevoked();
        String operationToken = getOperationToken();
        if (decreaseCallRequired) {
            log.info("order has expired, reserving order storage again");
            decreaseOrderStorageFuture = CompletableFuture.runAsync(() ->
                    productStorageService.decreaseOrderStorage(customerOrder.getProductSummary(), operationToken), customExecutor
            );
            customerOrder.setRevoked(Boolean.FALSE);
            customerOrder.setExpired(Boolean.FALSE);
            customerOrder.updateModifiedByUserAt();
            allDoneFuture = CompletableFuture.allOf(paymentQRLinkFuture, decreaseOrderStorageFuture);
        } else {
            allDoneFuture = CompletableFuture.allOf(paymentQRLinkFuture);
        }
        String paymentLink;
        try {
            allDoneFuture.get();
            paymentLink = paymentQRLinkFuture.get();
            orderRepository.saveAndFlush(customerOrder);
        } catch (InterruptedException | ExecutionException e) {
            log.error("error during place order again", e);
            if (decreaseCallRequired) {
                log.error("revoke last operation", e);
                // order expired
                CompletableFuture.runAsync(() ->
                        productStorageService.revokeOrderStorageChange(operationToken), customExecutor
                );
                if (decreaseOrderStorageFuture.isCompletedExceptionally()) {
                    throw new OrderStorageDecreaseException();
                }
            } else {
                // order not expired
            }
            if (paymentQRLinkFuture.isCompletedExceptionally()) {
                throw new PaymentQRLinkGenerationException();
            }
            throw new OrderCreationUnknownException();
        }
        return new OrderPaymentLinkRepresentation(paymentLink);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String authUserId, Long profileId, DeleteOrderCustomerCommand deleteOrderCustomerCommand) {
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, deleteOrderCustomerCommand.orderId);
        orderRepository.delete(customerOrder);
    }

    @Transactional
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")
    public void releaseExpiredOrder() {
        String changeId = getOperationToken();
        log.info("Expired order scheduler started, optToken generated {}", changeId);
        long l = Instant.now().toEpochMilli() - expireAfter * 60 * 1000;
        Instant instant = Instant.ofEpochMilli(l);
        Date from = Date.from(instant);
        List<CustomerOrder> expiredOrderList = orderRepository.findUnpaidExpiredNonRevokedOrders(from);
        List<Long> collect = expiredOrderList.stream().map(e -> e.getId()).collect(Collectors.toList());
        log.info("Expired order(s) found {}", collect.toString());
        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
        expiredOrderList.forEach(expiredOrder -> {
            Map<String, Integer> orderProductMap = expiredOrder.getProductSummary();
            orderProductMap.forEach((key, value) -> {
                stringIntegerHashMap.merge(key, value, Integer::sum);
            });
        });
        try {
            if (!stringIntegerHashMap.keySet().isEmpty()) {
                log.info("Release product(s) in order(s) :: " + stringIntegerHashMap.toString());
                productStorageService.increaseOrderStorage(stringIntegerHashMap, changeId);
                /** mark revoked orders */
                expiredOrderList.forEach(revokedOrder -> {
                    revokedOrder.setExpired(Boolean.TRUE);
                    revokedOrder.setRevoked(Boolean.TRUE);
                });
            }
            log.info("Expired order(s) released");
            orderRepository.saveAll(expiredOrderList);
            orderRepository.flush();
        } catch (Exception ex) {
            log.error("Error during release storage, revoke last operation", ex);
            CompletableFuture.runAsync(() ->
                    productStorageService.revokeOrderStorageChange(changeId)
            );
            throw new OrderSchedulerProductRecycleException();
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
