package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.Address;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class AddressRepresentationTest {
    @Test
    public void address_representation() {
        Address address = new Address();
        address.setCity(rStr());
        AddressRepresentation addressRepresentation = new AddressRepresentation(address);
        Assert.assertEquals(address.getCity(), addressRepresentation.getCity());
    }

    String rStr() {
        return UUID.randomUUID().toString();
    }
}