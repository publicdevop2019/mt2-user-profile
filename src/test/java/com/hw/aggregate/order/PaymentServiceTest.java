package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

import static com.hw.aggregate.Helper.rStr;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
//    @InjectMocks
//    PaymentService paymentService;
//    @Mock
//    ResourceServiceTokenHelper mock;
//    @Mock
//    EurekaRegistryHelper mock2;
//    @Mock
//    ObjectMapper mock3;
//
//    @Test
//    public void generatePaymentLink() throws JsonProcessingException {
//        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
//        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
//        Mockito.doReturn(null).when(resp).getBody();
//        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
//        Mockito.doReturn("dummy").when(mock3).writeValueAsString(any(Object.class));
//        String s = paymentService.generatePaymentLink(rStr(), rStr());
//        Assert.assertNull(s);
//    }
//
//    @Test
//    public void generatePaymentLink_json_ex() throws JsonProcessingException {
//        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
//        JsonProcessingException mock = Mockito.mock(JsonProcessingException.class);
//        Mockito.doReturn(resp).when(this.mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
//        Mockito.doReturn(null).when(resp).getBody();
//        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
//
//        Mockito.doThrow(mock).when(mock3).writeValueAsString(any(Object.class));
//        String s = paymentService.generatePaymentLink(rStr(), rStr());
//        Assert.assertNull(s);
//    }
//
//    @Test
//    public void generatePaymentLink_success() throws JsonProcessingException {
//        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
//        JsonProcessingException mock = Mockito.mock(JsonProcessingException.class);
//        Mockito.doReturn(resp).when(this.mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
//        Mockito.doReturn(new HashMap<>()).when(resp).getBody();
//        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
//
//        Mockito.doThrow(mock).when(mock3).writeValueAsString(any(Object.class));
//        String s = paymentService.generatePaymentLink(rStr(), rStr());
//        Assert.assertNull(s);
//    }
//
//    @Test
//    public void confirmPaymentStatus() {
//        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
//        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
//        Mockito.doReturn(null).when(resp).getBody();
//        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
//        Boolean aBoolean = paymentService.confirmPaymentStatus(rStr());
//        Assert.assertNull(aBoolean);
//    }
//
//    @Test
//    public void confirmPaymentStatus_success() {
//        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
//        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
//        Mockito.doReturn(new HashMap<>()).when(resp).getBody();
//        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
//        Boolean aBoolean = paymentService.confirmPaymentStatus(rStr());
//        Assert.assertNull(aBoolean);
//    }
}