package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.CartItem;
import lombok.Data;

import java.util.List;

@Data
public class CartSummaryRepresentation {

    private List<CartItem> cartItems;

    public CartSummaryRepresentation(List<CartItem> cartList) {
        cartItems = cartList;
    }
}
