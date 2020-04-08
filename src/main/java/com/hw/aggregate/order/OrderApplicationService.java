package com.hw.aggregate.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.exception.OrderAccessException;
import com.hw.aggregate.order.exception.OrderNotExistException;
import com.hw.aggregate.order.exception.OrderSchedulerProductRecycleException;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderPaymentStatus;
import com.hw.aggregate.order.representation.*;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public OrderRepresentation getOrderById(String authUserId, Long profileId, Long orderId) {
        return new OrderRepresentation(getOrderForCustomer(profileId, orderId));
    }

    /**
     * step0 validate order info
     * step1 add new order to profile
     * step2 generate payment link
     * step3 deduct amount from product service
     *
     * @note move decreaseStorage as late as possible bcz when code after decreaseStorage throw exception, then revoke storage required
     */
    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderPaymentLinkRepresentation reserveOrder(String authUserId, Long profileId, CreateOrderCommand newOrder) {
        log.debug("start of reserve order");
        CustomerOrder customerOrder = CustomerOrder.create(profileId, newOrder.getProductList(), newOrder.getAddress(), newOrder.getPaymentType(), newOrder.getPaymentAmt());

        // validate order
        customerOrder.validatePaymentAmount();
        productStorageService.validateProductInfo(customerOrder.getProductList());
        log.debug("order validation success");

        // generate order id, use db generate orderId
        CustomerOrder save = orderRepository.save(customerOrder);
        String reservedOrderId = save.getId().toString();
        log.debug("order id generated & saved");

        // generate payment QR link
        String paymentLink = paymentService.generatePaymentLink(reservedOrderId);
        log.debug("payment link generated");

        // decrease order storage
        productStorageService.decreaseOrderStorage(customerOrder.getProductSummary());
        log.debug("order storage decreased");
        cartApplicationService.clearCartItem(profileId);
        return new OrderPaymentLinkRepresentation(paymentLink);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public OrderConfirmStatusRepresentation confirmOrderPaymentStatus(String authUserId, Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        CustomerOrder customerOrder = getOrderForCustomer(profileId, confirmOrderPaymentCommand.orderId);

        Boolean paymentStatus = paymentService.confirmPaymentStatus(confirmOrderPaymentCommand.orderId.toString());
        customerOrder.setPaymentStatus(paymentStatus);
        if (paymentStatus)
            productStorageService.decreaseActualStorage(customerOrder.getProductSummary());

        orderRepository.save(customerOrder);

        log.debug("notify business owner asynchronously");
        messengerService.notifyBusinessOwner(new HashMap<>());

        OrderConfirmStatusRepresentation confirmStatusRepresentation = new OrderConfirmStatusRepresentation();
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
        CustomerOrder customerOrder = getOrderForCustomer(profileId, orderId);

        customerOrder.setAddress(placeOrderAgainCommand.getAddress());
        customerOrder.setPaymentType(placeOrderAgainCommand.getPaymentType());
        customerOrder.setExpired(Boolean.FALSE);
        customerOrder.updateModifiedByUserAt();

        String paymentLink = paymentService.generatePaymentLink(String.valueOf(orderId));

        if (customerOrder.getExpired() && customerOrder.getRevoked()) {
            productStorageService.decreaseOrderStorage(customerOrder.getProductSummary());
            customerOrder.setRevoked(Boolean.FALSE);
            orderRepository.save(customerOrder);
        }
        return new OrderPaymentLinkRepresentation(paymentLink);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteOrder(String authUserId, Long profileId, DeleteOrderCustomerCommand deleteOrderCustomerCommand) {
        CustomerOrder customerOrder = getOrderForCustomer(profileId, deleteOrderCustomerCommand.orderId);
        orderRepository.delete(customerOrder);
    }

    @Transactional
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")
    public void releaseExpiredOrder() {
        log.info("start of scheduler");
        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
        List<CustomerOrder> all = orderRepository.findAll();
        Stream<CustomerOrder> unpaidOrders = all.stream().filter(e -> e.getPaymentStatus().equals(CustomerOrderPaymentStatus.unpaid));
        Stream<CustomerOrder> expiredOrders = unpaidOrders.filter(e -> (e.getModifiedByUserAt().toInstant().toEpochMilli() + expireAfter * 60 * 1000 < Instant.now().toEpochMilli()) && (Boolean.FALSE.equals(e.getRevoked())));
        List<CustomerOrder> expiredOrderList = expiredOrders.collect(Collectors.toList());
        expiredOrderList.forEach(expiredOrder -> {
            expiredOrder.setExpired(Boolean.TRUE);
            expiredOrder.setRevoked(Boolean.FALSE);
            Map<String, Integer> orderProductMap = expiredOrder.getProductSummary();
            orderProductMap.forEach((key, value) -> {
                stringIntegerHashMap.merge(key, value, Integer::sum);
            });
        });
        try {
            if (!stringIntegerHashMap.keySet().isEmpty()) {
                log.info("release product(s) :: " + stringIntegerHashMap.toString());
                productStorageService.increaseOrderStorage(stringIntegerHashMap);
                /** mark revoked orders */
                expiredOrderList.forEach(revokedOrder -> {
                    revokedOrder.setRevoked(Boolean.TRUE);
                });
            }
        } catch (Exception ex) {
            log.error("error during revoking storage");
            throw new OrderSchedulerProductRecycleException();
        }
        expiredOrderList.forEach(e -> {
            orderRepository.save(e);
        });
        log.debug("order scheduler execute success");
    }

    private CustomerOrder getOrderForCustomer(Long profileId, Long orderId) {
        Optional<CustomerOrder> byId = orderRepository.findById(orderId);
        if (byId.isEmpty())
            throw new OrderNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new OrderAccessException();
        return byId.get();
    }
}
