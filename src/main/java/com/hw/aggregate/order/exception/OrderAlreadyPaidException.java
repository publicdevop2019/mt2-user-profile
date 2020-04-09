package com.hw.aggregate.order.exception;

import com.hw.shared.BadRequestException;

public class OrderAlreadyPaidException extends BadRequestException {
    public OrderAlreadyPaidException() {
        super("");
    }
}
