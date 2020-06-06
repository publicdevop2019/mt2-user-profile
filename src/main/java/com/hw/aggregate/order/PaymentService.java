package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class PaymentService {

    @Value("${url.confirmUrl}")
    private String confirmUrl;

    @Value("${url.paymentUrl}")
    private String paymentUrl;

    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    @Autowired
    private ObjectMapper mapper;

    public void rollbackTransaction(String transactionId) {

    }

    public String generatePaymentLink(String orderId) {
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
        String result = null;
        if (exchange.getBody() != null) {
            result = exchange.getBody().get("paymentLink");
        } else {
            log.error("unable to extract payment link from response");
        }
        return result;

    }

    public Boolean confirmPaymentStatus(String orderId) {
        log.debug("start of confirm payment status");
        ParameterizedTypeReference<HashMap<String, Boolean>> responseType =
                new ParameterizedTypeReference<>() {
                };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(headers);
        ResponseEntity<HashMap<String, Boolean>> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + confirmUrl + "/" + orderId, HttpMethod.GET, hashMapHttpEntity, responseType);
        Boolean result = null;
        if (exchange.getBody() != null) {
            result = exchange.getBody().get("paymentStatus");
        } else {
            log.error("unable to extract paymentStatus from response");
        }
        return result;

    }
}
