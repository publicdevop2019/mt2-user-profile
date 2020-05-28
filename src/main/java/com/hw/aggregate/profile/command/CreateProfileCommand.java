package com.hw.aggregate.profile.command;

import lombok.Data;

@Data
public class CreateProfileCommand {
    private String authorization;

    public CreateProfileCommand(String authorization) {
        this.authorization = authorization;
    }
}
