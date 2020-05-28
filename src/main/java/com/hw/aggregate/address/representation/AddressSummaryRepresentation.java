package com.hw.aggregate.address.representation;

import com.hw.aggregate.address.model.Address;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class AddressSummaryRepresentation {
    private List<AddressSummaryCustomerRepresentation> addressList;

    public AddressSummaryRepresentation(List<Address> addressList) {
        this.addressList = addressList.stream().map(AddressSummaryRepresentation.AddressSummaryCustomerRepresentation::new).collect(Collectors.toList());
    }

    @Data
    public class AddressSummaryCustomerRepresentation {
        private Long id;
        private String fullName;
        private String line1;
        private String line2;
        private String postalCode;
        private String phoneNumber;
        private String city;
        private String province;
        private String country;

        public AddressSummaryCustomerRepresentation(Address address) {
            this.id = address.getId();
            this.fullName = address.getFullName();
            this.line1 = address.getLine1();
            this.line2 = address.getLine2();
            this.postalCode = address.getPostalCode();
            this.phoneNumber = address.getPhoneNumber();
            this.city = address.getCity();
            this.province = address.getProvince();
            this.country = address.getCountry();
        }
    }
}
