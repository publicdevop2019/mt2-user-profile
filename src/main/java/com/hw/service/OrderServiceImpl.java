package com.hw.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.clazz.PaymentStatus;
import com.hw.clazz.ProductOption;
import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;
import com.hw.entity.SnapshotProduct;
import com.hw.repo.OrderService;
import com.hw.repo.ProfileRepo;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@EnableScheduling
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RestTemplate restTemplate;

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
    ProfileRepo profileRepo;

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
    @Override
    public String reserveOrder(OrderDetail nextOrderDetail, Profile profile) throws RuntimeException {

        validateOrderInfo(nextOrderDetail);

        if (profile.getOrderList() == null)
            profile.setOrderList(new ArrayList<>());
        List<OrderDetail> orderList = profile.getOrderList();
        int beforeInsert = orderList.size();

        nextOrderDetail.setPaymentStatus(PaymentStatus.unpaid);
        resetOrderSchedulerInfo(nextOrderDetail);
        orderList.add(nextOrderDetail);
        Map<String, Integer> productMap = getOrderProductMap(nextOrderDetail);
        decreaseStorage(productMap);
        String reservedOrderId;
        try {
            Profile save = profileRepo.save(profile);
            reservedOrderId = save.getOrderList().get(beforeInsert).getId().toString();
        } catch (Exception ex) {
            /**
             * when order failed on DB create
             */
            log.error("unable to create order", ex);
            increaseStorage(productMap);
            throw new RuntimeException("unable to create order");
        }
        return generatePaymentLink(reservedOrderId);

    }

    @Override
    public Boolean confirmOrder(String profileId, String orderId) throws RuntimeException {
        ParameterizedTypeReference<HashMap<String, Boolean>> responseType =
                new ParameterizedTypeReference<>() {
                };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        /**
         * get jwt token
         */
        if (tokenHelper.storedJwtToken == null)
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
        headers.setBearerAuth(tokenHelper.storedJwtToken);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(headers);
        ResponseEntity<HashMap<String, Boolean>> exchange;
        try {
            exchange = restTemplate.exchange(confirmUrl + "/" + orderId, HttpMethod.GET, hashMapHttpEntity, responseType);
        } catch (HttpClientErrorException ex) {
            /**
             * re-try if jwt expires
             */
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
            headers.setBearerAuth(tokenHelper.storedJwtToken);
            HttpEntity<String> hashMapHttpEntity2 = new HttpEntity<>(headers);
            exchange = restTemplate.exchange(confirmUrl + "/" + orderId, HttpMethod.GET, hashMapHttpEntity2, responseType);
        }
        Boolean paymentStatus = exchange.getBody().get("paymentStatus");
        if (paymentStatus) {
            decreaseActualStorage(getOrderProductMap(getOrder(Long.parseLong(profileId), Long.parseLong(orderId))));
        }
        return paymentStatus;

    }

    /***
     * @note only address and payment type can be updated
     * @param updatedOrder
     * @param orderId
     * @param profileId
     * @return
     */
    @Override
    public String replaceOrder(OrderDetail updatedOrder, long orderId, long profileId) {

        Optional<Profile> findById = profileRepo.findById(profileId);

        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId() == orderId).collect(Collectors.toList());
        OrderDetail oldOrder = collect.get(0);
        BeanUtils.copyProperties(updatedOrder.getAddress(), oldOrder.getAddress());
        oldOrder.setPaymentType(updatedOrder.getPaymentType());
        oldOrder.setModifiedByUserAt(Date.from(Instant.now()));
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
        return generatePaymentLink(String.valueOf(orderId));
    }


    @Override
    public Profile updateOrderById(String profileId, String orderId, OrderDetail updatedOrder) throws RuntimeException {
        updatedOrder.setId(Long.parseLong(orderId));
        Optional<Profile> findById = profileRepo.findById(Long.parseLong(profileId));
        OrderDetail oldOrder = getOrder(Long.parseLong(profileId), Long.parseLong(orderId));
        BeanUtils.copyProperties(updatedOrder, oldOrder);
        return profileRepo.save(findById.get());
    }

    private void resetOrderSchedulerInfo(OrderDetail orderDetail) {
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
        /**
         * get jwt token
         */
        if (tokenHelper.storedJwtToken == null)
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
        headers.setBearerAuth(tokenHelper.storedJwtToken);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<HashMap<String, String>> exchange;
        try {
            exchange = restTemplate.exchange(paymentUrl, HttpMethod.POST, hashMapHttpEntity, responseType);
        } catch (HttpClientErrorException ex) {
            /**
             * re-try if jwt expires
             */
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
            headers.setBearerAuth(tokenHelper.storedJwtToken);
            HttpEntity<String> hashMapHttpEntity2 = new HttpEntity<>(body, headers);
            exchange = restTemplate.exchange(paymentUrl, HttpMethod.POST, hashMapHttpEntity2, responseType);
        }
        return exchange.getBody().get("paymentLink");
    }

    public Map<String, Integer> getOrderProductMap(OrderDetail orderDetail) {
        /**
         * deduct product storage, this is a performance bottleneck with sync http
         */
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        orderDetail.getProductList().forEach(e -> {
            int defaultAmount = 1;
            if (e.getSelectedOptions() != null) {
                Optional<ProductOption> qty = e.getSelectedOptions().stream().filter(el -> el.title.equals("qty")).findFirst();
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

    private void validateOrderInfo(OrderDetail orderDetail) throws RuntimeException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        /**
         * get jwt token
         */
        if (tokenHelper.storedJwtToken == null)
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
        headers.setBearerAuth(tokenHelper.storedJwtToken);
        HttpEntity<List<SnapshotProduct>> hashMapHttpEntity = new HttpEntity<>(orderDetail.getProductList(), headers);
        ResponseEntity<HashMap<String, String>> exchange;
        ParameterizedTypeReference<HashMap<String, String>> responseType =
                new ParameterizedTypeReference<>() {
                };
        try {
            exchange = restTemplate.exchange(validateUrl, HttpMethod.POST, hashMapHttpEntity, responseType);
        } catch (HttpClientErrorException ex) {
            /**
             * re-try if jwt expires
             */
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
            headers.setBearerAuth(tokenHelper.storedJwtToken);
            HttpEntity<List<SnapshotProduct>> hashMapHttpEntity2 = new HttpEntity<>(orderDetail.getProductList(), headers);
            exchange = restTemplate.exchange(validateUrl, HttpMethod.POST, hashMapHttpEntity2, responseType);
        }
        if (exchange.getBody() == null || !"true".equals(exchange.getBody().get("result")))
            throw new RuntimeException("order validation failed");
        BigDecimal reduce = orderDetail.getProductList().stream().map(e -> BigDecimal.valueOf(Double.parseDouble(e.getFinalPrice()))).reduce(BigDecimal.valueOf(0), BigDecimal::add);
        if (orderDetail.getPaymentAmt().compareTo(reduce) != 0)
            throw new RuntimeException("invalid payment amount");
    }

    @Override
    public void decreaseStorage(Map<String, Integer> productMap) {
        changeStorage(decreaseUrl, productMap);
    }

    @Override
    public void increaseStorage(Map<String, Integer> productMap) {
        changeStorage(increaseUrl, productMap);
    }

    @Override
    public void decreaseActualStorage(Map<String, Integer> productMap) {
        changeStorage(soldUrl, productMap);
    }

    /**
     * @param contentMap
     * @todo generify
     */
    @Async
    @Override
    public void notifyBusinessOwner(Map<String, String> contentMap) {
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
        /**
         * get jwt token
         */
        if (tokenHelper.storedJwtToken == null)
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
        headers.setBearerAuth(tokenHelper.storedJwtToken);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(body, headers);
        try {
            restTemplate.exchange(notifyUrl, HttpMethod.POST, hashMapHttpEntity, String.class);
        } catch (HttpClientErrorException ex) {
            /**
             * re-try if jwt expires
             */
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
            headers.setBearerAuth(tokenHelper.storedJwtToken);
            HttpEntity<String> hashMapHttpEntity2 = new HttpEntity<>(body, headers);
            restTemplate.exchange(notifyUrl, HttpMethod.POST, hashMapHttpEntity2, String.class);
        }

    }

    @Override
    @Transactional
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")
    public void releaseExpiredOrder() {
        log.info("start of scheduler");
        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
        List<Profile> all = profileRepo.findAll();
        all.forEach(p -> {
            Stream<OrderDetail> unpaidOrders = p.getOrderList().stream().filter(e -> e.getPaymentStatus().equals(PaymentStatus.unpaid));
            Stream<OrderDetail> expiredOrder = unpaidOrders.filter(e -> (e.getModifiedByUserAt().toInstant().toEpochMilli() + expireAfter * 60 * 1000 < Instant.now().toEpochMilli()) && (Boolean.FALSE.equals(e.getRevoked())));
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
                    Stream<OrderDetail> unpaidOrders = p.getOrderList().stream().filter(e -> e.getPaymentStatus().equals(PaymentStatus.unpaid));
                    Stream<OrderDetail> expiredOrder = unpaidOrders.filter(e -> e.getExpired().equals(Boolean.TRUE) && e.getRevoked().equals(Boolean.FALSE));
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
        /**
         * get jwt token
         */
        if (tokenHelper.storedJwtToken == null)
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
        headers.setBearerAuth(tokenHelper.storedJwtToken);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(body, headers);
        try {
            restTemplate.exchange(url, HttpMethod.PUT, hashMapHttpEntity, String.class);
        } catch (HttpClientErrorException ex) {
            /**
             * re-try if jwt expires
             */
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
            headers.setBearerAuth(tokenHelper.storedJwtToken);
            HttpEntity<String> hashMapHttpEntity2 = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PUT, hashMapHttpEntity2, String.class);
        }
    }

    private OrderDetail getOrder(long profileId, long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId() == orderId).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new RuntimeException("id should be unique");
        return collect.get(0);
    }
}
