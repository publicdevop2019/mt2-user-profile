package com.hw.aggregate.order.model;

import com.hw.aggregate.order.CustomerOrderRepository;
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
        CustomerOrderRepository mock = Mockito.mock(CustomerOrderRepository.class);
        Mockito.doReturn(Optional.empty()).when(mock).findByIdPesLock(anyLong());
        CustomerOrder customerOrder = CustomerOrder.get(rLong(), rLong(), mock);
    }

    @Test(expected = OrderAccessException.class)
    public void get_wrong_access() {
        CustomerOrderRepository mock = Mockito.mock(CustomerOrderRepository.class);
        CustomerOrder mock1 = Mockito.mock(CustomerOrder.class);
        Mockito.doReturn(Optional.of(mock1)).when(mock).findByIdPesLock(anyLong());
        Mockito.doReturn(100L).when(mock1).getProfileId();
        CustomerOrder customerOrder = CustomerOrder.get(rLong(), rLong(), mock);
    }

    @Test
    public void get() {
        CustomerOrderRepository mock = Mockito.mock(CustomerOrderRepository.class);
        CustomerOrder mock1 = Mockito.mock(CustomerOrder.class);
        Mockito.doReturn(Optional.of(mock1)).when(mock).findByIdPesLock(anyLong());
        Long aLong = rLong();
        Mockito.doReturn(aLong).when(mock1).getProfileId();
        CustomerOrder customerOrder = CustomerOrder.get(aLong, rLong(), mock);
        Assert.assertNotNull(customerOrder.getId());
    }
}