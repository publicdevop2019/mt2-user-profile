package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.BizAddress;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class BizAddressRepresentationTest {
    @Test
    public void address_representation() {
//        BizAddress address = new BizAddress();
//        address.setCity(rStr());
//        AddressRepresentation addressRepresentation = new AddressRepresentation(address);
//        Assert.assertEquals(address.getCity(), addressRepresentation.getCity());
    }

    String rStr() {
        return UUID.randomUUID().toString();
    }
}