package com.hw.aggregate.cart.exception;

import com.hw.shared.BadRequestException;

public class CartItemNotExistException extends BadRequestException {
    public CartItemNotExistException() {
        super("");
    }
}
