package com.hw.aggregate.order.exception;

import com.hw.shared.BadRequestException;

public class OrderAccessException extends BadRequestException {

    public OrderAccessException() {
        super("");
    }
}
