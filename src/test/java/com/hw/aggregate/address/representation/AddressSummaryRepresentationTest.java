package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.Address;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class AddressSummaryRepresentationTest {

    @Test
    public void getAddressList() {
        ArrayList<Address> addresses = new ArrayList<>();
        AddressSummaryRepresentation addressSummaryRepresentation = new AddressSummaryRepresentation(addresses);
        Assert.assertEquals(0, addressSummaryRepresentation.getAddressList().size());
    }
}