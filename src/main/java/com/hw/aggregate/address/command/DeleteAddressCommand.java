package com.hw.aggregate.address.command;

import lombok.Data;

@Data
public class DeleteAddressCommand {
    private Long addressId;

    public DeleteAddressCommand(Long addressId) {
        this.addressId = addressId;
    }
}
