//package com.hw.aggregate.cart.model;
//
//import com.hw.aggregate.cart.BizCartRepository;
//import com.hw.aggregate.cart.command.UserCreateBizCartItemCommand;
//import com.hw.aggregate.cart.exception.CartItemAccessException;
//import com.hw.aggregate.cart.exception.CartItemNotExistException;
//import com.hw.aggregate.cart.exception.MaxCartItemException;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Optional;
//
//import static com.hw.aggregate.Helper.rLong;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//
//public class BizCartTest {
//
//    @Test(expected = MaxCartItemException.class)
//    public void create() {
//        BizCartRepository mock = Mockito.mock(BizCartRepository.class);
//        ArrayList<BizCartItem> cartItems = new ArrayList<>();
//        ArrayList<BizCartItem> spy = Mockito.spy(cartItems);
//        Mockito.doReturn(spy).when(mock).findByProfileId(anyLong());
//        Mockito.doReturn(10).when(spy).size();
//
//        BizCartItem.create(rLong(), rLong(), new UserCreateBizCartItemCommand(), mock);
//    }
//
//    @Test
//    public void create_success() {
//        BizCartRepository mock = Mockito.mock(BizCartRepository.class);
//        ArrayList<BizCartItem> cartItems = new ArrayList<>();
//        ArrayList<BizCartItem> spy = Mockito.spy(cartItems);
//        Mockito.doReturn(spy).when(mock).findByProfileId(anyLong());
//        Mockito.doReturn(0).when(spy).size();
//        Mockito.doReturn(new BizCartItem()).when(mock).save(any(BizCartItem.class));
//        UserCreateBizCartItemCommand createCartItemCommand = new UserCreateBizCartItemCommand();
//        createCartItemCommand.setAttrIdMap(new HashMap<>());
//        BizCartItem cartItem = BizCartItem.create(rLong(), rLong(), createCartItemCommand, mock);
//        Assert.assertNotNull(cartItem);
//    }
//
//    @Test(expected = CartItemNotExistException.class)
//    public void get_not_exist() {
//        BizCartRepository repo = Mockito.mock(BizCartRepository.class);
//        Mockito.doReturn(Optional.empty()).when(repo).findById(anyLong());
//        BizCartItem cartItem = BizCartItem.get(rLong(), rLong(), repo);
//    }
//
//    @Test(expected = CartItemAccessException.class)
//    public void get_wrong_access() {
//        BizCartRepository repo = Mockito.mock(BizCartRepository.class);
//        BizCartItem cartItem1 = new BizCartItem();
//        cartItem1.setProfileId(rLong());
//        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
//        BizCartItem cartItem = BizCartItem.get(rLong(), rLong(), repo);
//    }
//
//    @Test
//    public void get() {
//        BizCartRepository repo = Mockito.mock(BizCartRepository.class);
//        BizCartItem cartItem1 = new BizCartItem();
//        Long aLong = rLong();
//        cartItem1.setProfileId(aLong);
//        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
//        BizCartItem cartItem = BizCartItem.get(aLong, rLong(), repo);
//        Assert.assertNotNull(cartItem);
//    }
//
//    @Test(expected = CartItemAccessException.class)
//    public void delete_wrong_access() {
//        BizCartRepository repo = Mockito.mock(BizCartRepository.class);
//        BizCartItem cartItem1 = new BizCartItem();
//        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
//        cartItem1.setProfileId(rLong());
//        BizCartItem.delete(rLong(), rLong(), repo);
//    }
//
//    @Test
//    public void delete() {
//        BizCartRepository repo = Mockito.mock(BizCartRepository.class);
//        BizCartItem cartItem1 = new BizCartItem();
//        Long aLong = rLong();
//        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
//        cartItem1.setProfileId(aLong);
//        BizCartItem.delete(aLong, rLong(), repo);
//        Mockito.verify(repo, Mockito.times(1)).deleteById(anyLong());
//    }
//}