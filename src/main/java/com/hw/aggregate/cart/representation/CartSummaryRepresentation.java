package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.CartItem;

import java.util.List;

public class CartSummaryRepresentation {

    public List<CartItem> cartItems;

    public CartSummaryRepresentation(List<CartItem> cartList) {
        cartItems = cartList;
    }
}
