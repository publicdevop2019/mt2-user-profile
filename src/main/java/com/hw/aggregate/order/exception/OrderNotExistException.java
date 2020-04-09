package com.hw.aggregate.order.exception;

import com.hw.shared.BadRequestException;

public class OrderNotExistException extends BadRequestException {
    public OrderNotExistException() {
        super("");
    }
}
