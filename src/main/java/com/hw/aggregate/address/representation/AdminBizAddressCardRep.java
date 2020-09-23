package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.BizAddress;
import lombok.Data;

import java.util.Date;

@Data
public class AdminBizAddressCardRep {
    private Long id;

    private String fullName;

    private String postalCode;

    private String phoneNumber;

    private String city;

    private String province;

    private String country;
    private long createdAt;

    public AdminBizAddressCardRep(BizAddress bizAddress) {
        this.id = bizAddress.getId();
        this.fullName = bizAddress.getFullName();
        this.postalCode = bizAddress.getPostalCode();
        this.phoneNumber = bizAddress.getPhoneNumber();
        this.city = bizAddress.getCity();
        this.province = bizAddress.getProvince();
        this.country = bizAddress.getCountry();
        this.createdAt = bizAddress.getCreatedAt().getTime();
    }

}
