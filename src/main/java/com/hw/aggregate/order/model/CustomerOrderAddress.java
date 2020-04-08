package com.hw.aggregate.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOrderAddress {

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String fullName;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String line1;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String line2;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String postalCode;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String phoneNumber;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String city;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String province;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String country;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerOrderAddress address = (CustomerOrderAddress) o;
        return Objects.equals(fullName, address.fullName) &&
                Objects.equals(line1, address.line1) &&
                Objects.equals(line2, address.line2) &&
                Objects.equals(postalCode, address.postalCode) &&
                Objects.equals(phoneNumber, address.phoneNumber) &&
                Objects.equals(city, address.city) &&
                Objects.equals(province, address.province) &&
                Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, line1, line2, postalCode, phoneNumber, city, province, country);
    }
}
