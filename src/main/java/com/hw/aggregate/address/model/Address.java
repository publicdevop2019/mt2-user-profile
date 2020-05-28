package com.hw.aggregate.address.model;

import com.hw.aggregate.address.command.CreateAddressCommand;
import com.hw.shared.Auditable;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "Address")
@SequenceGenerator(name = "addressId_gen", sequenceName = "addressId_gen", initialValue = 100)
@Data
@NoArgsConstructor
public class Address extends Auditable {
    @Id
    private Long id;

    @Column(name = "fk_profile")
    private Long profileId;

    @NotNull
    @NotEmpty
    private String fullName;

    @NotNull
    @NotEmpty
    private String line1;

    @NotNull
    @NotEmpty
    private String line2;

    @NotNull
    @NotEmpty
    private String postalCode;

    @NotNull
    @NotEmpty
    private String phoneNumber;

    @NotNull
    @NotEmpty
    private String city;

    @NotNull
    @NotEmpty
    private String province;

    @NotNull
    @NotEmpty
    private String country;

    public static Address create(Long id, Long profileId, CreateAddressCommand command) {
        return new Address(id, profileId, command);
    }

    private Address(Long id, Long profileId, CreateAddressCommand command) {
        this.id = id;
        this.profileId = profileId;
        this.fullName = command.getFullName();
        this.line1 = command.getLine1();
        this.line2 = command.getLine2();
        this.postalCode = command.getPostalCode();
        this.phoneNumber = command.getPhoneNumber();
        this.city = command.getCity();
        this.province = command.getProvince();
        this.country = command.getCountry();
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
                Objects.equals(profileId, address.profileId) &&
                Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, line1, line2, postalCode, phoneNumber, city, province, country, profileId);
    }
}
