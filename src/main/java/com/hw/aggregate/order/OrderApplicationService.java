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
    public OrderPaymentLinkRepresentation createNew(String authUserId, Long profileId, CreateOrderCommand newOrder) {
        CustomerOrder customerOrder = CustomerOrder.create(profileId, newOrder.getProductList(), newOrder.getAddress(), newOrder.getPaymentType(), newOrder.getPaymentAmt());

        // validate order product info
        CompletableFuture<Void> validateResultFuture = CompletableFuture.runAsync(() ->
                productStorageService.validateProductInfo(customerOrder.getReadOnlyProductList()), customExecutor
        );

        // generate order id, use db generate orderId
        CustomerOrder save = orderRepository.save(customerOrder);
        String reservedOrderId = save.getId().toString();
        log.debug("order id {} generated", reservedOrderId);

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
            save.setPaymentLink(paymentLink);
        } catch (InterruptedException | ExecutionException e) {
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
            throw new OrderCreationUnknownException();
        }
        log.debug("order storage decreased");
        cartApplicationService.clearCartItem(profileId);
        return new OrderPaymentLinkRepresentation(paymentLink, Boolean.FALSE);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderConfirmStatusRepresentation confirmPayment(String authUserId, Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, confirmOrderPaymentCommand.orderId);
        Boolean paymentStatus = paymentService.confirmPaymentStatus(confirmOrderPaymentCommand.orderId.toString());
        if (paymentStatus) {
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
    @Transactional
    public void confirmOrder(String authUserId, Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, confirmOrderPaymentCommand.orderId);
        customerOrder.toConfirmed();
        CompletableFuture<Void> decreaseActualStorageFuture = CompletableFuture.runAsync(() ->
                productStorageService.decreaseActualStorage(customerOrder.getProductSummary(), customerOrder.getId().toString()), customExecutor
        );
        try {
            decreaseActualStorageFuture.get();
            log.info("notify business owner asynchronously");
            orderRepository.saveAndFlush(customerOrder);
            messengerService.notifyBusinessOwner(new HashMap<>());
        } catch (InterruptedException | ExecutionException e) {
            log.error("error during confirm order", e);
            if (decreaseActualStorageFuture.isCompletedExceptionally())
                // actual storage will not revoked
                // when user confirm again, decreaseActualStorage api is idempotent with same orderId
                throw new ActualStorageDecreaseException();
        }
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
            customerOrder.toPaidReserved();
            if (placeOrderAgainCommand != null && placeOrderAgainCommand.getAddress() != null) {
                log.info("updating order address");
                customerOrder.setAddress(placeOrderAgainCommand.getAddress());
            }
            representation = new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), Boolean.TRUE);
        } else if (customerOrder.getOrderState().equals(OrderState.NOT_PAID_RECYCLED)) {
            customerOrder.toNotPaidReserved();
            if (placeOrderAgainCommand != null && (placeOrderAgainCommand.getPaymentType() != null || placeOrderAgainCommand.getAddress() != null)) {
                log.info("updating order address & paymentType if applicable");
                if (placeOrderAgainCommand.getAddress() != null)
                    customerOrder.setAddress(placeOrderAgainCommand.getAddress());
                if (placeOrderAgainCommand.getPaymentType() != null)
                    customerOrder.setPaymentType(placeOrderAgainCommand.getPaymentType());
            }
            representation = new OrderPaymentLinkRepresentation(customerOrder.getPaymentLink(), Boolean.FALSE);
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

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String authUserId, Long profileId, DeleteOrderCustomerCommand deleteOrderCustomerCommand) {
        CustomerOrder customerOrder = getOrderForCustomerToUpdate(profileId, deleteOrderCustomerCommand.orderId);
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
            orderProductMap.forEach((key, value) -> {
                stringIntegerHashMap.merge(key, value, Integer::sum);
            });
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

    @Transactional
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.resubmit}")
    public void resubmitOrder() {
        String changeId = getOperationToken();
        log.info("Resubmit order scheduler started, optToken generated {}", changeId);
        List<CustomerOrder> paidReserved = orderRepository.findPaidReserved();
        List<Long> collect = paidReserved.stream().map(CustomerOrder::getId).collect(Collectors.toList());
        log.info("Paid reserved order(s) found {}", collect.toString());
        try {
            if (collect.size() > 0) {
                // submit one order for now
                // @TODO batch job ??
                CustomerOrder customerOrder = paidReserved.get(0);
                log.info("Resubmit order {}", customerOrder.getId());
                confirmOrder(null, customerOrder.getProfileId(), new ConfirmOrderPaymentCommand(customerOrder.getId()));
            }
            log.info("Paid reserved order released");
        } catch (Exception ex) {
            log.error("Error during resubmit order", ex);
            throw new OrderResubmitException();
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
