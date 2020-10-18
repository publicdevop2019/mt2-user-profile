package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.BizAddress;
import lombok.Data;

@Data
public class AdminBizAddressRep {
    private Long id;

    private String fullName;

    private String line1;

    private String line2;

    private String postalCode;

    private String phoneNumber;

    private String city;

    private String province;

    private String country;
    private String createdBy;
    private long createdAt;
    private String modifiedBy;
    private long modifiedAt;

    public AdminBizAddressRep(BizAddress bizAddress) {
        this.id = bizAddress.getId();
        this.fullName = bizAddress.getFullName();
        this.line1 = bizAddress.getLine1();
        this.line2 = bizAddress.getLine2();
        this.postalCode = bizAddress.getPostalCode();
        this.phoneNumber = bizAddress.getPhoneNumber();
        this.city = bizAddress.getCity();
        this.province = bizAddress.getProvince();
        this.country = bizAddress.getCountry();
        this.modifiedAt = bizAddress.getModifiedAt().getTime();
        this.modifiedBy = bizAddress.getModifiedBy();
        this.createdAt = bizAddress.getCreatedAt().getTime();
        this.createdBy = bizAddress.getCreatedBy();
    }
}
