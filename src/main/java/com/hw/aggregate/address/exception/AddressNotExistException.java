package com.hw.aggregate.address.exception;

import com.hw.shared.BadRequestException;

public class AddressNotExistException extends BadRequestException {
    public AddressNotExistException() {
        super("");
    }
}
