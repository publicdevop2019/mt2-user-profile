package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.CartItem;
import lombok.Data;

@Data
public class CartItemCreatedRepresentation {
    private String cartItemId;

    public CartItemCreatedRepresentation(CartItem cartItem) {
        cartItemId = cartItem.getId().toString();
    }
}
