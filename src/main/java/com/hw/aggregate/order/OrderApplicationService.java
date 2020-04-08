package com.hw.aggregate.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.exception.OrderNotExistException;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderPaymentStatus;
import com.hw.aggregate.order.representation.*;
import com.hw.aggregate.profile.ProfileRepo;
import com.hw.aggregate.profile.model.Profile;
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
import java.util.*;
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
    private ProfileRepo profileRepo;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productStorageService;

    @Autowired
    private MessengerService messengerService;

    @Transactional(readOnly = true)
    public OrderSummaryAdminRepresentation getAllOrdersForAdmin() {
        List<CustomerOrder> collect = profileRepo.findAll().stream().map(Profile::getOrderList).flatMap(Collection::stream).collect(Collectors.toList());
        return new OrderSummaryAdminRepresentation(collect);
    }

    @Transactional(readOnly = true)
    public OrderSummaryCustomerRepresentation getAllOrders(Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        return new OrderSummaryCustomerRepresentation(profileByResourceOwnerId.get().getOrderList());
    }

    @Transactional(readOnly = true)
    public OrderRepresentation getOrderById(Long profileId, Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new OrderNotExistException();
        return new OrderRepresentation(collect.get(0));
    }

    /**
     * step0 validate order info
     * step1 add new order to profile
     * step2 generate payment link
     * step3 deduct amount from product service
     *
     * @note move decreaseStorage as late as possible bcz when code after decreaseStorage throw exception, then revoke storage required
     */
    @Transactional
    public OrderPaymentLinkRepresentation reserveOrder(Long profileId, ReserveOrderCommand newOrder) {
        log.debug("start of reserve order");
        CustomerOrder customerOrder = CustomerOrder.create(newOrder.getProductList(), newOrder.getAddress(), newOrder.getPaymentType(), newOrder.getPaymentAmt());

        // validate order
        customerOrder.validatePaymentAmount();
        productStorageService.validateProductInfo(customerOrder.getProductList());
        log.debug("order validation success");

        // generate order id, use db generate orderId
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.get().getOrderList() == null)
            findById.get().setOrderList(new ArrayList<>());
        List<CustomerOrder> orderList = findById.get().getOrderList();
        int beforeInsert = orderList.size();
        orderList.add(customerOrder);
        Profile save = profileRepo.save(findById.get());
        String reservedOrderId = save.getOrderList().get(beforeInsert).getId().toString();
        log.debug("order id generated & saved");

        // generate payment QR link
        String paymentLink = paymentService.generatePaymentLink(reservedOrderId);
        log.debug("payment link generated");

        // decrease order storage
        productStorageService.decreaseOrderStorage(customerOrder.getProductSummary());
        log.debug("order storage decreased");
        // @todo move to cart
        /**
         * clear shopping cart
         */
        findById.get().getCartList().clear();
        profileRepo.save(findById.get());
        return new OrderPaymentLinkRepresentation(paymentLink);
    }

    @Transactional
    public OrderConfirmStatusRepresentation confirmOrderPaymentStatus(Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(confirmOrderPaymentCommand.orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new OrderNotExistException();
        CustomerOrder customerOrder = collect.get(0);

        Boolean paymentStatus = paymentService.confirmPaymentStatus(confirmOrderPaymentCommand.orderId.toString());
        customerOrder.setPaymentStatus(paymentStatus);
        if (paymentStatus)
            productStorageService.decreaseActualStorage(customerOrder.getProductSummary());

        profileRepo.save(findById.get());

        log.debug("notify business owner asynchronously");
        messengerService.notifyBusinessOwner(new HashMap<>());

        OrderConfirmStatusRepresentation confirmStatusRepresentation = new OrderConfirmStatusRepresentation();
        confirmStatusRepresentation.put("paymentStatus", paymentStatus);
        return confirmStatusRepresentation;
    }

    @Transactional
    public void updateOrderAdmin(Long profileId, Long orderId, UpdateOrderAdminCommand newOrder) {
        newOrder.setId(orderId);
        Optional<Profile> findById = profileRepo.findById(profileId);
        CustomerOrder oldOrder = getOrder(profileId, orderId);
        BeanUtils.copyProperties(newOrder, oldOrder);
        profileRepo.save(findById.get());
    }

    /**
     * only address and payment_method can be updated
     */
    @Transactional
    public OrderPaymentLinkRepresentation placeOrderAgain(Long profileId, Long orderId, PlaceOrderAgainCommand placeOrderAgainCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new OrderNotExistException();

        CustomerOrder oldOrder = collect.get(0);

        oldOrder.setAddress(placeOrderAgainCommand.getAddress());
        oldOrder.setPaymentType(placeOrderAgainCommand.getPaymentType());
        oldOrder.setExpired(Boolean.FALSE);
        oldOrder.updateModifiedByUserAt();

        String paymentLink = paymentService.generatePaymentLink(String.valueOf(orderId));

        if (oldOrder.getExpired() && oldOrder.getRevoked()) {
            productStorageService.decreaseOrderStorage(oldOrder.getProductSummary());
            oldOrder.setRevoked(Boolean.FALSE);
            profileRepo.save(findById.get());
        }
        return new OrderPaymentLinkRepresentation(paymentLink);
    }

    @Transactional
    public void deleteOrder(Long profileId, DeleteOrderCustomerCommand deleteOrderCustomerCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(deleteOrderCustomerCommand.orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new OrderNotExistException();

        CustomerOrder toBeRemoved = collect.get(0);
        findById.get().getOrderList().removeIf(e -> e.getId().equals(toBeRemoved.getId()));
        profileRepo.save(findById.get());
    }

    @Transactional
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")
    public void releaseExpiredOrder() {
        log.info("start of scheduler");
        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
        List<Profile> all = profileRepo.findAll();
        all.forEach(p -> {
            Stream<CustomerOrder> unpaidOrders = p.getOrderList().stream().filter(e -> e.getPaymentStatus().equals(CustomerOrderPaymentStatus.unpaid));
            Stream<CustomerOrder> expiredOrder = unpaidOrders.filter(e -> (e.getModifiedByUserAt().toInstant().toEpochMilli() + expireAfter * 60 * 1000 < Instant.now().toEpochMilli()) && (Boolean.FALSE.equals(e.getRevoked())));
            expiredOrder.forEach(or -> {
                or.setExpired(Boolean.TRUE);
                or.setRevoked(Boolean.FALSE);
                Map<String, Integer> orderProductMap = or.getProductSummary();
                orderProductMap.forEach((key, value) -> {
                    stringIntegerHashMap.merge(key, value, Integer::sum);
                });
            });
            profileRepo.save(p);

        });
        try {
            if (!stringIntegerHashMap.keySet().isEmpty()) {
                log.info("release product(s) :: " + stringIntegerHashMap.toString());
                productStorageService.increaseOrderStorage(stringIntegerHashMap);
                /** mark revoked orders */
                all.forEach(p -> {
                    Stream<CustomerOrder> unpaidOrders = p.getOrderList().stream().filter(e -> e.getPaymentStatus().equals(CustomerOrderPaymentStatus.unpaid));
                    Stream<CustomerOrder> expiredOrder = unpaidOrders.filter(e -> e.getExpired().equals(Boolean.TRUE) && e.getRevoked().equals(Boolean.FALSE));
                    expiredOrder.forEach(or -> {
                        or.setRevoked(Boolean.TRUE);
                    });
                    profileRepo.save(p);
                });
            }
        } catch (Exception ex) {
            log.error("error during revoking storage");

        }
    }

    private CustomerOrder getOrder(long profileId, long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId() == orderId).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new OrderNotExistException();
        return collect.get(0);
    }
}
