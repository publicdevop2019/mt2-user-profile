package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.exception.PaymentQRLinkGenerationException;
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
        if (null != exchange.getBody() && null != exchange.getBody().get("paymentLink")) {
            return exchange.getBody().get("paymentLink");
        } else {
            throw new PaymentQRLinkGenerationException();
        }
    }

    public Boolean confirmPaymentStatus(String orderId) {
        log.debug("start of confirm order");
        ParameterizedTypeReference<HashMap<String, Boolean>> responseType =
                new ParameterizedTypeReference<>() {
                };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(headers);
        ResponseEntity<HashMap<String, Boolean>> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + confirmUrl + "/" + orderId, HttpMethod.GET, hashMapHttpEntity, responseType);
        Boolean paymentStatus = exchange.getBody().get("paymentStatus");
        return paymentStatus;

    }
}
