package com.hw.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "Address")
@SequenceGenerator(name = "addressId_gen", sequenceName = "addressId_gen", initialValue = 100)
@Data
public class Address extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "addressId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

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
        Address address = (Address) o;
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
