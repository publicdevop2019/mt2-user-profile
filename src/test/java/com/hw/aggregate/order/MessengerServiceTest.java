package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class MessengerServiceTest {

    @InjectMocks
    MessengerService messengerService;
    @Mock
    ResourceServiceTokenHelper mock;
    @Mock
    EurekaRegistryHelper mock2;
    @Mock
    ObjectMapper mock3;

    @Test
    public void notifyBusinessOwner() throws JsonProcessingException {
        ResponseEntity mock1 = Mockito.mock(ResponseEntity.class);
        Mockito.doReturn(mock1).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doReturn("dummy").when(mock3).writeValueAsString(any(Object.class));
        messengerService.notifyBusinessOwner(new HashMap<>());
        Mockito.verify(mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void notifyBusinessOwner_throw_ex() throws JsonProcessingException {
        ResponseEntity mock1 = Mockito.mock(ResponseEntity.class);
        JsonProcessingException mock4 = Mockito.mock(JsonProcessingException.class);
        Mockito.doReturn(mock1).when(mock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        Mockito.doReturn("dummy").when(mock2).getProxyHomePageUrl();
        Mockito.doThrow(mock4).when(mock3).writeValueAsString(any(Object.class));
        messengerService.notifyBusinessOwner(new HashMap<>());
        Mockito.verify(mock, Mockito.times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }
}