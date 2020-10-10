package com.hw.aggregate.order.command;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizOrderAddressCmdRep implements Serializable {
    private static final long serialVersionUID = 1;
    private String fullName;
    private String line1;
    private String line2;
    private String postalCode;
    private String phoneNumber;
    private String city;
    private String province;
    private String country;

}
