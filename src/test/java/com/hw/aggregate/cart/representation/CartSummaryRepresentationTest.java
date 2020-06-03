package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.CartItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class CartSummaryRepresentationTest {

    @Test
    public void getCartItems() {
        ArrayList<CartItem> cartItems = new ArrayList<>();
        CartSummaryRepresentation cartSummaryRepresentation = new CartSummaryRepresentation(cartItems);
        Assert.assertEquals(0, cartSummaryRepresentation.getCartItems().size());
    }
}