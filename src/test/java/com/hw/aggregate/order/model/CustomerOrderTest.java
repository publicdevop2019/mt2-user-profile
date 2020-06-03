package com.hw.aggregate.order.model;

import com.hw.aggregate.order.OrderRepository;
import com.hw.aggregate.order.exception.OrderAccessException;
import com.hw.aggregate.order.exception.OrderNotExistException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static com.hw.aggregate.Helper.rLong;
import static org.mockito.ArgumentMatchers.anyLong;

public class CustomerOrderTest {

    @Test(expected = OrderNotExistException.class)
    public void get_not_exist() {
        OrderRepository mock = Mockito.mock(OrderRepository.class);
        Mockito.doReturn(Optional.empty()).when(mock).findById(anyLong());
        CustomerOrder customerOrder = CustomerOrder.get(rLong(), rLong(), mock);
    }

    @Test(expected = OrderAccessException.class)
    public void get_wrong_access() {
        OrderRepository mock = Mockito.mock(OrderRepository.class);
        CustomerOrder mock1 = Mockito.mock(CustomerOrder.class);
        Mockito.doReturn(Optional.of(mock1)).when(mock).findById(anyLong());
        Mockito.doReturn(100L).when(mock1).getProfileId();
        CustomerOrder customerOrder = CustomerOrder.get(rLong(), rLong(), mock);
    }

    @Test
    public void get() {
        OrderRepository mock = Mockito.mock(OrderRepository.class);
        CustomerOrder mock1 = Mockito.mock(CustomerOrder.class);
        Mockito.doReturn(Optional.of(mock1)).when(mock).findById(anyLong());
        Long aLong = rLong();
        Mockito.doReturn(aLong).when(mock1).getProfileId();
        CustomerOrder customerOrder = CustomerOrder.get(aLong, rLong(), mock);
        Assert.assertNotNull(customerOrder.getId());
    }
}