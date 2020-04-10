package com.hw.aggregate.cart.exception;

import com.hw.shared.BadRequestException;

public class MaxCartItemException extends BadRequestException {
    public MaxCartItemException() {
        super("");
    }
}
