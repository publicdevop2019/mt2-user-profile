package com.hw.aggregate.profile.exception;

import com.hw.shared.BadRequestException;

public class ProfileNotExistException extends BadRequestException {
    public ProfileNotExistException() {
        super("");
    }
}
