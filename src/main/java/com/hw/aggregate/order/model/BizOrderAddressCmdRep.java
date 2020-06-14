package com.hw.aggregate.order.model;

import lombok.Data;

@Data
public class BizOrderAddressCmdRep {

    private String fullName;
    private String line1;
    private String line2;
    private String postalCode;
    private String phoneNumber;
    private String city;
    private String province;
    private String country;

}