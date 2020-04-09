package com.hw.aggregate.order.exception;

import com.hw.shared.BadRequestException;

public class ProductInfoValidationException extends BadRequestException {
    public ProductInfoValidationException() {
        super("");
    }
}
