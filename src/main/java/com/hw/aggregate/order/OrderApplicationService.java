package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.exception.OrderNotExistException;
import com.hw.aggregate.order.exception.OrderPaymentMismatchException;
import com.hw.aggregate.order.exception.OrderValidationException;
import com.hw.aggregate.order.exception.PaymentQRLinkGenerationException;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderItem;
import com.hw.aggregate.order.model.CustomerOrderItemAddOn;
import com.hw.aggregate.order.model.CustomerOrderPaymentStatus;
import com.hw.aggregate.order.representation.*;
import com.hw.aggregate.profile.ProfileRepo;
import com.hw.aggregate.profile.model.Profile;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.InternalServerException;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Value("${url.decreaseUrl}")
    private String decreaseUrl;

    @Value("${url.sold}")
    private String soldUrl;

    @Value("${url.increaseUrl}")
    private String increaseUrl;

    @Value("${url.validateUrl}")
    private String validateUrl;

    @Value("${url.paymentUrl}")
    private String paymentUrl;

    @Value("${url.confirmUrl}")
    private String confirmUrl;

    @Value("${url.notify}")
    private String notifyUrl;

    @Value("${order.expireAfter}")
    private Long expireAfter;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    private ProfileRepo profileRepo;

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

    @Transactional
    public OrderPaymentLinkRepresentation reserveOrder(Long profileId, ReserveOrderCommand newOrder) {
        removeUnwantedValue(newOrder);
        String paymentLink;
        Optional<Profile> findById = profileRepo.findById(profileId);
        try {
            paymentLink = reserveOrder(newOrder, findById.get());
        } catch (RuntimeException ex) {
            log.error("unable to reserve order ", ex);
            throw new PaymentQRLinkGenerationException();
        }
        /**
         * clear shopping cart
         */
        findById.get().getCartList().clear();
        profileRepo.save(findById.get());
        return new OrderPaymentLinkRepresentation(paymentLink);
    }

    @Transactional
    public OrderConfirmStatusRepresentation confirmOrder(Long profileId, ConfirmOrderPaymentCommand confirmOrderPaymentCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(confirmOrderPaymentCommand.orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new OrderNotExistException();
        CustomerOrder orderDetail = collect.get(0);
        OrderConfirmStatusRepresentation confirmStatusRepresentation = new OrderConfirmStatusRepresentation();

        if (confirmOrder(profileId.toString(), confirmOrderPaymentCommand.orderId.toString())) {
            orderDetail.setPaymentStatus(CustomerOrderPaymentStatus.paid);
            confirmStatusRepresentation.put("paymentStatus", Boolean.TRUE);
        } else {
            orderDetail.setPaymentStatus(CustomerOrderPaymentStatus.unpaid);
            confirmStatusRepresentation.put("paymentStatus", Boolean.FALSE);
        }
        profileRepo.save(findById.get());
        try {
            Map<String, String> contentMap = new HashMap<>();
            /**
             * @todo add order details
             */
            notifyBusinessOwner(contentMap);
        } catch (Exception ex) {
            log.error("unable to notify business owner", ex);
        }
        return confirmStatusRepresentation;
    }

    /**
     * only address and payment_method can be updated
     */
    //@review
    @Transactional
    public void updateOrder(Long profileId, Long orderId, UpdateOrderAddressOrPaymentMethodCommand newOrder) {
        newOrder.setId(orderId);
        Optional<Profile> findById = profileRepo.findById(profileId);
        CustomerOrder oldOrder = getOrder(profileId, orderId);
        BeanUtils.copyProperties(newOrder, oldOrder);
        profileRepo.save(findById.get());
    }

    //@review
    @Transactional
    public OrderPaymentLinkRepresentation placeOrderAgain(Long profileId, Long orderId, PlaceOrderAgainCommand newOrder) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId() == orderId).collect(Collectors.toList());
        CustomerOrder oldOrder = collect.get(0);
        BeanUtils.copyProperties(newOrder.getAddress(), oldOrder.getAddress());
        oldOrder.setPaymentType(newOrder.getPaymentType());
        oldOrder.setModifiedByUserAt(Date.from(Instant.now()));
        String paymentLink = generatePaymentLink(String.valueOf(orderId));
        if (oldOrder.getExpired()) {
            oldOrder.setExpired(Boolean.FALSE);
            Map<String, Integer> productMap = getOrderProductMap(oldOrder);
            if (oldOrder.getRevoked())
                decreaseStorage(productMap);
            oldOrder.setRevoked(Boolean.FALSE);
            profileRepo.save(findById.get());
        } else {
            /**
             * storage not release yet,
             */
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

    /**
     * step0 validate order info
     * step1 deduct amount from product service
     * step2 add new order to profile
     * step3 generate payment link
     *
     * @param nextOrderDetail
     * @return payment QR link
     * @note if step2 or 3 does not complete successfully then revocation required
     */
    private String reserveOrder(CustomerOrder nextOrderDetail, Profile profile) throws RuntimeException {
        log.debug("start of reserve order");
        validateOrderInfo(nextOrderDetail);
        log.debug("order validation success");
        if (profile.getOrderList() == null)
            profile.setOrderList(new ArrayList<>());
        List<CustomerOrder> orderList = profile.getOrderList();
        int beforeInsert = orderList.size();

        nextOrderDetail.setPaymentStatus(CustomerOrderPaymentStatus.unpaid);
        resetOrderSchedulerInfo(nextOrderDetail);
        orderList.add(nextOrderDetail);
        Map<String, Integer> productMap = getOrderProductMap(nextOrderDetail);
        Profile save = profileRepo.save(profile);
        String reservedOrderId = save.getOrderList().get(beforeInsert).getId().toString();
        log.info("order save success");
        String paymentLink = generatePaymentLink(reservedOrderId);
        //move decreaseStorage as late as possible bcz when code after decreaseStorage throw exception, then revoke storage required
        log.debug("start of decrease product(s) order storage");
        decreaseStorage(productMap);
        return paymentLink;

    }

    private Boolean confirmOrder(String profileId, String orderId) throws RuntimeException {
        log.debug("start of confirm order");
        ParameterizedTypeReference<HashMap<String, Boolean>> responseType =
                new ParameterizedTypeReference<>() {
                };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(headers);
        ResponseEntity<HashMap<String, Boolean>> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + confirmUrl + "/" + orderId, HttpMethod.GET, hashMapHttpEntity, responseType);
        Boolean paymentStatus = exchange.getBody().get("paymentStatus");
        if (paymentStatus) {
            log.debug("order payment status is true, decrease actual storage");
            decreaseActualStorage(getOrderProductMap(getOrder(Long.parseLong(profileId), Long.parseLong(orderId))));
        }
        return paymentStatus;

    }


    private void resetOrderSchedulerInfo(CustomerOrder orderDetail) {
        orderDetail.setExpired(Boolean.FALSE);
        orderDetail.setRevoked(Boolean.FALSE);
        orderDetail.setModifiedByUserAt(Date.from(Instant.now()));
    }

    private String generatePaymentLink(String orderId) {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("orderId", orderId);
        String body = null;
        try {
            body = mapper.writeValueAsString(stringStringHashMap);
        } catch (JsonProcessingException e) {
            /**
             * this block is purposely left blank
             */
        }
        ParameterizedTypeReference<HashMap<String, String>> responseType =
                new ParameterizedTypeReference<>() {
                };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<HashMap<String, String>> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + paymentUrl, HttpMethod.POST, hashMapHttpEntity, responseType);
        if (null != exchange.getBody() && null != exchange.getBody().get("paymentLink")) {
            log.info("payment link generate success");
            return exchange.getBody().get("paymentLink");
        } else {
            log.info("payment link generate failed");
            throw new InternalServerException("error during payment link generation");
        }
    }

    private Map<String, Integer> getOrderProductMap(CustomerOrder orderDetail) {
        /**
         * deduct product storage, this is a performance bottleneck with sync http
         */
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        orderDetail.getProductList().forEach(e -> {
            int defaultAmount = 1;
            if (e.getSelectedOptions() != null) {
                Optional<CustomerOrderItemAddOn> qty = e.getSelectedOptions().stream().filter(el -> el.title.equals("qty")).findFirst();
                if (qty.isPresent() && !qty.get().options.isEmpty()) {
                    /**
                     * deduct amount based on qty value, otherwise default is 1
                     */
                    defaultAmount = Integer.parseInt(qty.get().options.get(0).optionValue);
                }
            }
            if (stringIntegerHashMap.containsKey(e.getProductId())) {
                stringIntegerHashMap.put(e.getProductId(), stringIntegerHashMap.get(e.getProductId()) + defaultAmount);
            } else {
                stringIntegerHashMap.put(e.getProductId(), defaultAmount);
            }
        });
        return stringIntegerHashMap;
    }

    private void validateOrderInfo(CustomerOrder orderDetail) throws RuntimeException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<CustomerOrderItem>> hashMapHttpEntity = new HttpEntity<>(orderDetail.getProductList(), headers);
        ParameterizedTypeReference<HashMap<String, String>> responseType =
                new ParameterizedTypeReference<>() {
                };
        ResponseEntity<HashMap<String, String>> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + validateUrl, HttpMethod.POST, hashMapHttpEntity, responseType);
        if (exchange.getBody() == null || !"true".equals(exchange.getBody().get("result")))
            throw new OrderValidationException();
        BigDecimal reduce = orderDetail.getProductList().stream().map(e -> BigDecimal.valueOf(Double.parseDouble(e.getFinalPrice()))).reduce(BigDecimal.valueOf(0), BigDecimal::add);
        if (orderDetail.getPaymentAmt().compareTo(reduce) != 0)
            throw new OrderPaymentMismatchException();
    }

    private void decreaseStorage(Map<String, Integer> productMap) {
        changeStorage(decreaseUrl, productMap);
    }

    private void increaseStorage(Map<String, Integer> productMap) {
        changeStorage(increaseUrl, productMap);
    }

    private void decreaseActualStorage(Map<String, Integer> productMap) {
        changeStorage(soldUrl, productMap);
    }

    /**
     * @param contentMap
     * @todo generify
     */
    @Async
    private void notifyBusinessOwner(Map<String, String> contentMap) {
        String body = null;
        try {
            body = mapper.writeValueAsString(contentMap);
        } catch (JsonProcessingException e) {
            /**
             * this block is purposely left blank
             */
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(body, headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + notifyUrl, HttpMethod.POST, hashMapHttpEntity, String.class);

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
                Map<String, Integer> orderProductMap = getOrderProductMap(or);
                orderProductMap.forEach((key, value) -> {
                    stringIntegerHashMap.merge(key, value, Integer::sum);
                });
            });
            profileRepo.save(p);

        });
        try {
            if (!stringIntegerHashMap.keySet().isEmpty()) {
                log.info("release product(s) :: " + stringIntegerHashMap.toString());
                increaseStorage(stringIntegerHashMap);
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

    private void changeStorage(String url, Map<String, Integer> productMap) {
        String body = null;
        try {
            body = mapper.writeValueAsString(productMap);
        } catch (JsonProcessingException e) {
            /**
             * this block is purposely left blank
             */
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(body, headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + url, HttpMethod.PUT, hashMapHttpEntity, String.class);
    }

    private CustomerOrder getOrder(long profileId, long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId() == orderId).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new RuntimeException("id should be unique");
        return collect.get(0);
    }

    private void removeUnwantedValue(CustomerOrder orderDetail) {
        orderDetail.setId(null);
    }
}
