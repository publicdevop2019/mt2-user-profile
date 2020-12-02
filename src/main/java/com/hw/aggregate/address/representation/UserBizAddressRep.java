package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.BizAddress;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class UserBizAddressRep {
    private Long id;

    private String fullName;

    private String line1;

    private String line2;

    private String postalCode;

    private String phoneNumber;

    private String city;

    private String province;

    private String country;

    public UserBizAddressRep(BizAddress bizAddress) {
        BeanUtils.copyProperties(bizAddress, this);
    }
}
