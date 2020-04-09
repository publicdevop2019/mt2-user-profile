package com.hw.aggregate.address.exception;

import com.hw.shared.BadRequestException;

public class MaxAddressCountException extends BadRequestException {
    public MaxAddressCountException() {
        super("");
    }
}
