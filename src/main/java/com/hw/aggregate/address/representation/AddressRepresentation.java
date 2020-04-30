package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.Address;
import lombok.Data;

@Data
public class AddressRepresentation {
    private Long id;
    private String fullName;
    private String line1;
    private String line2;
    private String postalCode;
    private String phoneNumber;
    private String city;
    private String province;
    private String country;

    public AddressRepresentation(Address addressForCustomer) {
        this.id = addressForCustomer.getId();
        this.fullName = addressForCustomer.getFullName();
        this.line1 = addressForCustomer.getLine1();
        this.line2 = addressForCustomer.getLine2();
        this.postalCode = addressForCustomer.getPostalCode();
        this.phoneNumber = addressForCustomer.getPhoneNumber();
        this.city = addressForCustomer.getCity();
        this.province = addressForCustomer.getProvince();
        this.country = addressForCustomer.getCountry();
    }
}
