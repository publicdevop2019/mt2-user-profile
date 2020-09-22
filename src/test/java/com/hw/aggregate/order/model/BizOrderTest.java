//package com.hw.aggregate.order.model;
//
//import com.hw.aggregate.order.BizOrderRepository;
//import com.hw.aggregate.order.exception.BizOrderAccessException;
//import com.hw.aggregate.order.exception.BizOrderNotExistException;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import java.util.Optional;
//
//import static com.hw.aggregate.Helper.rLong;
//import static org.mockito.ArgumentMatchers.anyLong;
//
//public class BizOrderTest {
//
//    @Test(expected = BizOrderNotExistException.class)
//    public void get_not_exist() {
//        BizOrderRepository mock = Mockito.mock(BizOrderRepository.class);
//        Mockito.doReturn(Optional.empty()).when(mock).findById(anyLong());
//        BizOrder customerOrder = BizOrder.get(rLong(), rLong(), mock);
//    }
//
//    @Test(expected = BizOrderAccessException.class)
//    public void get_wrong_access() {
//        BizOrderRepository mock = Mockito.mock(BizOrderRepository.class);
//        BizOrder mock1 = Mockito.mock(BizOrder.class);
//        Mockito.doReturn(Optional.of(mock1)).when(mock).findById(anyLong());
//        Mockito.doReturn(100L).when(mock1).getProfileId();
//        BizOrder customerOrder = BizOrder.get(rLong(), rLong(), mock);
//    }
//
//    @Test
//    public void get() {
//        BizOrderRepository mock = Mockito.mock(BizOrderRepository.class);
//        BizOrder mock1 = Mockito.mock(BizOrder.class);
//        Mockito.doReturn(Optional.of(mock1)).when(mock).findById(anyLong());
//        Long aLong = rLong();
//        Mockito.doReturn(aLong).when(mock1).getProfileId();
//        BizOrder customerOrder = BizOrder.get(aLong, rLong(), mock);
//        Assert.assertNotNull(customerOrder.getId());
//    }
//}