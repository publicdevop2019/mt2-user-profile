package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.CartItem;
import org.junit.Assert;
import org.junit.Test;

import static com.hw.aggregate.Helper.rLong;

public class CartItemCreatedRepresentationTest {

    @Test
    public void getCartItemId() {
        CartItem cartItem = new CartItem();
        Long aLong = rLong();
        cartItem.setId(aLong);
        CartItemCreatedRepresentation cartItemRepresentation = new CartItemCreatedRepresentation(cartItem);
        Assert.assertEquals(cartItemRepresentation.getCartItemId(), cartItem.getId().toString());
    }
}