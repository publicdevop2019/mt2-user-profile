package com.hw.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.clazz.ProductOption;
import com.hw.clazz.ResourceServiceTokenHelper;
import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;
import com.hw.repo.OrderService;
import com.hw.repo.ProfileRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    ProfileRepo profileRepo;

    /**
     * step1 deduct amount from product service
     * step2 add new order to profile
     * step3 clear shopping cart
     *
     * @param orderDetail
     * @return order detail id
     * @note if step2 or 3 does not complete successfully then revocation required
     */
    @Override
    @Transactional
    public String placeOrder(OrderDetail orderDetail, Profile profile) {
        if (profile.getOrderList() == null)
            profile.setOrderList(new ArrayList<>());
        List<OrderDetail> orderList = profile.getOrderList();
        int beforeInsert = orderList.size();
        orderList.add(orderDetail);
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
        decreaseStorage(stringIntegerHashMap);
        Profile save = null;
        try {
            profile.getCartList().clear();
            save = profileRepo.save(profile);
        } catch (Exception ex) {
            log.error("Error during order placement, revoking product storage", ex);
            increaseStorage(stringIntegerHashMap);
        }
        return save.getOrderList().get(beforeInsert).getId().toString();
    }

    @Override
    public void decreaseStorage(Map<String, Integer> productMap) {
        changeStorage(decreaseUrl, productMap);
    }

    @Override
    public void increaseStorage(Map<String, Integer> productMap) {
        changeStorage(increaseUrl, productMap);
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
