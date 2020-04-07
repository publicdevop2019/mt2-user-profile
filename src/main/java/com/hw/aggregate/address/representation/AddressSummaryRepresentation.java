package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.Address;

import java.util.List;

public class AddressSummaryRepresentation {
    public List<Address> addressList;

    public AddressSummaryRepresentation(List<Address> addressList) {
        this.addressList = addressList;
    }
}
