package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.aggregate.order.model.CustomerOrderItem;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

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

    @Value("${url.revoke}")
    private String revokeUrl;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;


    public void decreaseOrderStorage(Map<String, Integer> productMap, String optToken) {
        changeStorage(decreaseUrl, productMap, optToken);
    }

    public void increaseOrderStorage(Map<String, Integer> productMap, String optToken) {
        changeStorage(increaseUrl, productMap, optToken);
    }

    public void rollbackChange(String optToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + revokeUrl + "?optToken=" + optToken, HttpMethod.PUT, hashMapHttpEntity, String.class);
    }

    public void decreaseActualStorage(Map<String, Integer> productMap, String optToken) {
        changeStorage(soldUrl, productMap, optToken);
    }

    private void changeStorage(String url, Map<String, Integer> productMap, String optToken) {
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
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + url + "?optToken=" + optToken, HttpMethod.PUT, hashMapHttpEntity, String.class);
    }

    public void validateProductInfo(List<CustomerOrderItem> customerOrderItemList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<CustomerOrderItem>> hashMapHttpEntity = new HttpEntity<>(customerOrderItemList, headers);
        ParameterizedTypeReference<HashMap<String, String>> responseType =
                new ParameterizedTypeReference<>() {
                };
        ResponseEntity<HashMap<String, String>> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + validateUrl, HttpMethod.POST, hashMapHttpEntity, responseType);
        if (exchange.getBody() == null || !"true".equals(exchange.getBody().get("result")))
            throw new ProductInfoValidationException();
    }

}
