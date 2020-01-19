package com.hw.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.clazz.PaymentStatus;
import com.hw.clazz.ProductOption;
import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;
import com.hw.entity.SnapshotProduct;
import com.hw.exceptions.OrderValidationException;
import com.hw.repo.OrderService;
import com.hw.repo.ProfileRepo;
import com.hw.utility.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.decreaseUrl}")
    private String decreaseUrl;

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

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    ProfileRepo profileRepo;

    /**
     * step0 validate payment amount
     * step1 deduct amount from product service
     * step2 add new order to profile
     * step3 clear shopping cart
     *
     * @param orderDetail
     * @return payment QR link
     * @note if step2 or 3 does not complete successfully then revocation required
     */
    @Override
    public String reserveOrder(OrderDetail orderDetail, Profile profile) throws OrderValidationException {
        if (profile.getOrderList() == null)
            profile.setOrderList(new ArrayList<>());
        List<OrderDetail> orderList = profile.getOrderList();
        int beforeInsert = orderList.size();

        orderDetail.setPaymentStatus(PaymentStatus.unpaid);

        orderList.add(orderDetail);

        validateOrder(orderDetail);

        Map<String, Integer> productMap = getOrderProductMap(orderDetail);

        decreaseStorage(productMap);

        Profile save = profileRepo.save(profile);
        String reservedOrderId = save.getOrderList().get(beforeInsert).getId().toString();

        return generatePaymentLink(reservedOrderId);
    }

    @Override
    public Boolean confirmOrder(String orderId) throws OrderValidationException {
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
        return exchange.getBody().get("paymentStatus");
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

    private Map<String, Integer> getOrderProductMap(OrderDetail orderDetail) {
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

    private void validateOrder(OrderDetail orderDetail) throws OrderValidationException {
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
            throw new OrderValidationException();
    }

    @Override
    public void decreaseStorage(Map<String, Integer> productMap) {
        changeStorage(decreaseUrl, productMap);
    }

    @Override
    public void increaseStorage(Map<String, Integer> productMap) {
        changeStorage(increaseUrl, productMap);
    }

    /**
     * @param contentMap
     * @todo generify
     */
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
}
