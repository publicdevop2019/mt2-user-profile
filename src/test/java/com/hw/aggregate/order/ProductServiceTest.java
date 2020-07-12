package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
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

import java.util.ArrayList;
import java.util.HashMap;

import static com.hw.aggregate.Helper.rStr;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {
    @InjectMocks
    ProductService productService;
    @Mock
    ResourceServiceTokenHelper mock;
    @Mock
    EurekaRegistryHelper mock2;
    @Mock
    ObjectMapper mock3;

    @Test
    public void decreaseOrderStorage_json_ex() throws JsonProcessingException {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        JsonProcessingException mock = Mockito.mock(JsonProcessingException.class);
        Mockito.doReturn(resp).when(this.mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doThrow(mock).when(mock3).writeValueAsString(any(Object.class));
        productService.decreaseOrderStorage(new ArrayList<>(), rStr());
        Mockito.verify(this.mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }
    @Test
    public void decreaseOrderStorage() throws JsonProcessingException {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doReturn("dummy").when(mock3).writeValueAsString(any(Object.class));
        productService.decreaseOrderStorage(new ArrayList<>(), rStr());
        Mockito.verify(mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void increaseOrderStorage() throws JsonProcessingException {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doReturn("dummy").when(mock3).writeValueAsString(any(Object.class));
        productService.increaseOrderStorage(new ArrayList<>(), rStr());
        Mockito.verify(mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void decreaseActualStorage() throws JsonProcessingException {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doReturn("dummy").when(mock3).writeValueAsString(any(Object.class));
        productService.decreaseActualStorage(new ArrayList<>(), rStr());
        Mockito.verify(mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void rollbackChange() {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        productService.rollbackTransaction(rStr());
        Mockito.verify(mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test(expected = ProductInfoValidationException.class)
    public void validateProductInfo() {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doReturn(null).when(resp).getBody();
        productService.validateProductInfo(new ArrayList<>());
    }

    @Test(expected = ProductInfoValidationException.class)
    public void validateProductInfo2() {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doReturn(new HashMap<>()).when(resp).getBody();
        productService.validateProductInfo(new ArrayList<>());
    }

    @Test
    public void validateProductInfo_success() {
        ResponseEntity resp = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(resp).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("result", "true");
        Mockito.doReturn(objectObjectHashMap).when(resp).getBody();
        productService.validateProductInfo(new ArrayList<>());
        Mockito.verify(mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
}