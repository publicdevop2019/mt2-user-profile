package com.hw.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.clazz.ResourceServiceTokenHelper;
import com.hw.repo.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.productPart0}")
    private String part0;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;


    @Override
    public void deductAmount(Map<String, Integer> productMap) {
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
            restTemplate.exchange(part0, HttpMethod.PUT, hashMapHttpEntity, String.class);
        } catch (HttpClientErrorException ex) {
            /**
             * re-try
             */
            tokenHelper.storedJwtToken = tokenHelper.getJwtToken();
            headers.setBearerAuth(tokenHelper.storedJwtToken);
            HttpEntity<String> hashMapHttpEntity2 = new HttpEntity<>(body, headers);
            restTemplate.exchange(part0, HttpMethod.PUT, hashMapHttpEntity2, String.class);

        }
    }
}
