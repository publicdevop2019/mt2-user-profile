package com.hw.aggregate.order.exception;

import com.hw.shared.BadRequestException;

public class OrderPaymentMismatchException extends BadRequestException {

    public OrderPaymentMismatchException() {
        super("");
    }
}
