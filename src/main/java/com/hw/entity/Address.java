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
public class Address extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "addressId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String firstName;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String lastName;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String streetName;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String streetNumber;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String aptNumber;

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
        return Objects.equals(firstName, address.firstName) &&
                Objects.equals(lastName, address.lastName) &&
                Objects.equals(streetName, address.streetName) &&
                Objects.equals(streetNumber, address.streetNumber) &&
                Objects.equals(aptNumber, address.aptNumber) &&
                Objects.equals(postalCode, address.postalCode) &&
                Objects.equals(phoneNumber, address.phoneNumber) &&
                Objects.equals(city, address.city) &&
                Objects.equals(province, address.province) &&
                Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, streetName, streetNumber, aptNumber, postalCode, phoneNumber, city, province, country);
    }
}
