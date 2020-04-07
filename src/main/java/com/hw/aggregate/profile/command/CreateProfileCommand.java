package com.hw.aggregate.profile.command;

public class CreateProfileCommand {
    public String authorization;

    public CreateProfileCommand(String authorization) {
        this.authorization = authorization;
    }
}
