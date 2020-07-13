package com.hw.aggregate.cart.model;

import com.hw.aggregate.cart.CartRepository;
import com.hw.aggregate.cart.command.CreateCartItemCommand;
import com.hw.aggregate.cart.exception.CartItemAccessException;
import com.hw.aggregate.cart.exception.CartItemNotExistException;
import com.hw.aggregate.cart.exception.MaxCartItemException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static com.hw.aggregate.Helper.rLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class CartItemTest {

    @Test(expected = MaxCartItemException.class)
    public void create() {
        CartRepository mock = Mockito.mock(CartRepository.class);
        ArrayList<CartItem> cartItems = new ArrayList<>();
        ArrayList<CartItem> spy = Mockito.spy(cartItems);
        Mockito.doReturn(spy).when(mock).findByProfileId(anyLong());
        Mockito.doReturn(10).when(spy).size();

        CartItem.create(rLong(), rLong(), new CreateCartItemCommand(), mock);
    }

    @Test
    public void create_success() {
        CartRepository mock = Mockito.mock(CartRepository.class);
        ArrayList<CartItem> cartItems = new ArrayList<>();
        ArrayList<CartItem> spy = Mockito.spy(cartItems);
        Mockito.doReturn(spy).when(mock).findByProfileId(anyLong());
        Mockito.doReturn(0).when(spy).size();
        Mockito.doReturn(new CartItem()).when(mock).save(any(CartItem.class));
        CreateCartItemCommand createCartItemCommand = new CreateCartItemCommand();
        createCartItemCommand.setAttrIdMap(new HashMap<>());
        CartItem cartItem = CartItem.create(rLong(), rLong(), createCartItemCommand, mock);
        Assert.assertNotNull(cartItem);
    }

    @Test(expected = CartItemNotExistException.class)
    public void get_not_exist() {
        CartRepository repo = Mockito.mock(CartRepository.class);
        Mockito.doReturn(Optional.empty()).when(repo).findById(anyLong());
        CartItem cartItem = CartItem.get(rLong(), rLong(), repo);
    }

    @Test(expected = CartItemAccessException.class)
    public void get_wrong_access() {
        CartRepository repo = Mockito.mock(CartRepository.class);
        CartItem cartItem1 = new CartItem();
        cartItem1.setProfileId(rLong());
        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
        CartItem cartItem = CartItem.get(rLong(), rLong(), repo);
    }

    @Test
    public void get() {
        CartRepository repo = Mockito.mock(CartRepository.class);
        CartItem cartItem1 = new CartItem();
        Long aLong = rLong();
        cartItem1.setProfileId(aLong);
        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
        CartItem cartItem = CartItem.get(aLong, rLong(), repo);
        Assert.assertNotNull(cartItem);
    }

    @Test(expected = CartItemAccessException.class)
    public void delete_wrong_access() {
        CartRepository repo = Mockito.mock(CartRepository.class);
        CartItem cartItem1 = new CartItem();
        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
        cartItem1.setProfileId(rLong());
        CartItem.delete(rLong(), rLong(), repo);
    }

    @Test
    public void delete() {
        CartRepository repo = Mockito.mock(CartRepository.class);
        CartItem cartItem1 = new CartItem();
        Long aLong = rLong();
        Mockito.doReturn(Optional.of(cartItem1)).when(repo).findById(anyLong());
        cartItem1.setProfileId(aLong);
        CartItem.delete(aLong, rLong(), repo);
        Mockito.verify(repo, Mockito.times(1)).deleteById(anyLong());
    }
}