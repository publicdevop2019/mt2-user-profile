package com.hw.aggregate.address.command;

public class DeleteAddressCommand {
    public Long addressId;

    public DeleteAddressCommand(Long addressId) {
        this.addressId = addressId;
    }
}
