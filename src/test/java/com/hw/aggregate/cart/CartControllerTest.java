package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.CreateCartItemCommand;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.cart.representation.CartItemRepresentation;
import com.hw.aggregate.cart.representation.CartSummaryRepresentation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.hw.aggregate.Helper.rJwt;
import static com.hw.aggregate.Helper.rLong;
import static org.mockito.ArgumentMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class CartControllerTest {
    @InjectMocks
    CartController cartController;
    @Mock
    CartApplicationService cartApplicationService;

    @Test
    public void getCartItems() {
        CartSummaryRepresentation cartSummaryRepresentation = new CartSummaryRepresentation(List.of(new CartItem()));
        Mockito.doReturn(cartSummaryRepresentation).when(cartApplicationService).getCartItems(anyString(), anyLong());
        ResponseEntity<List<CartSummaryRepresentation.CartSummaryCardRepresentation>> cartItems = cartController.getCartItems(rJwt(), rLong());
        Assert.assertNotNull(cartItems.getBody());
    }

    @Test
    public void addCartItem() {
        CartItem cartItem = new CartItem();
        Long aLong = rLong();
        cartItem.setId(aLong);
        Mockito.doReturn(new CartItemRepresentation(cartItem)).when(cartApplicationService).addCartItem(anyString(), anyLong(), any(CreateCartItemCommand.class));
        ResponseEntity<Void> voidResponseEntity = cartController.addCartItem(rJwt(), rLong(), new CreateCartItemCommand());
        Assert.assertEquals(aLong.toString(), voidResponseEntity.getHeaders().getLocation().toString());

    }

    @Test
    public void deleteCartItem() {
        Mockito.doNothing().when(cartApplicationService).deleteCartItem(anyString(), anyLong(), anyLong());
        ResponseEntity<Void> voidResponseEntity = cartController.deleteCartItem(rJwt(), rLong(), rLong());
        Mockito.verify(cartApplicationService, Mockito.times(1)).deleteCartItem(anyString(), anyLong(), anyLong());
        Assert.assertNotNull(voidResponseEntity);
    }
}