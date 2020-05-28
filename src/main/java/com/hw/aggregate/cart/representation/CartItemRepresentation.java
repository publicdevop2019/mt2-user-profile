package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.CartItem;
import lombok.Data;

@Data
public class CartItemRepresentation {
    private String cartItemId;

    public CartItemRepresentation(CartItem cartItem) {
        cartItemId = cartItem.getId().toString();
    }
}
