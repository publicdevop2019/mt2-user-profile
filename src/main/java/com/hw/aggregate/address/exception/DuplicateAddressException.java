package com.hw.aggregate.address.exception;

import com.hw.shared.BadRequestException;

public class DuplicateAddressException extends BadRequestException {

    public DuplicateAddressException() {
        super("");
    }
}
