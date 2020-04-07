package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.CartItem;

public class CartItemRepresentation {
    public String cartItemId;

    public CartItemRepresentation(CartItem cartItem) {
        cartItemId = cartItem.getId().toString();
    }
}
