package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.BizAddress;
import lombok.Data;
import org.springframework.beans.BeanUtils;

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
        BeanUtils.copyProperties(bizAddress, this);
        this.createdAt = bizAddress.getCreatedAt().getTime();
    }

}
