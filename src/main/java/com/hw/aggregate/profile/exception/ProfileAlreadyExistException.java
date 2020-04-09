package com.hw.aggregate.profile.exception;

import com.hw.shared.InternalServerException;

public class ProfileAlreadyExistException extends InternalServerException {
    public ProfileAlreadyExistException() {
        super("");
    }
}
