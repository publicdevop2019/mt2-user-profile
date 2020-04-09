package com.hw.aggregate.address.model;

import com.hw.shared.Auditable;
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

    @Column(name = "fk_profile")
    private Long profileId;

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

    public Address() {

    }

    public static Address create(Long profileId, String fullName, String line1, String line2, String postalCode, String phoneNumber, String city, String province, String country) {
        return new Address(profileId, fullName, line1, line2, postalCode, phoneNumber, city, province, country);
    }

    private Address(Long profileId, String fullName, String line1, String line2, String postalCode, String phoneNumber, String city, String province, String country) {
        this.profileId = profileId;
        this.fullName = fullName;
        this.line1 = line1;
        this.line2 = line2;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.province = province;
        this.country = country;
    }

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
