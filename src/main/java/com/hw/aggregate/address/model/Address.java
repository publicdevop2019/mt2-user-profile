package com.hw.aggregate.address.model;

import com.hw.aggregate.address.AddressRepository;
import com.hw.aggregate.address.command.CreateAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.exception.AddressAccessException;
import com.hw.aggregate.address.exception.AddressNotExistException;
import com.hw.aggregate.address.exception.DuplicateAddressException;
import com.hw.aggregate.address.exception.MaxAddressCountException;
import com.hw.shared.Auditable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Column(nullable = false)
    private String line1;

    private String line2;

    @NotBlank
    @Column(nullable = false)
    private String postalCode;

    @NotBlank
    @Column(nullable = false)
    private String phoneNumber;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Column(nullable = false)
    private String province;

    @NotBlank
    @Column(nullable = false)
    private String country;

    public static Address create(Long id, Long profileId, CreateAddressCommand command, AddressRepository addressRepository) {
        List<Address> byProfileId = addressRepository.findByProfileId(profileId);
        if (byProfileId.size() == 5)
            throw new MaxAddressCountException();
        if (byProfileId.stream().anyMatch(e -> e.isDuplicateOf(command))) {
            throw new DuplicateAddressException();
        }
        return addressRepository.save(new Address(id, profileId, command));
    }

    public static Address get(Long profileId, Long addressId, AddressRepository addressRepository) {
        Optional<Address> byId = addressRepository.findById(addressId);
        if (byId.isEmpty())
            throw new AddressNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new AddressAccessException();
        return byId.get();
    }

    public static Address update(Long profileId, Long addressId, AddressRepository addressRepository, UpdateAddressCommand command) {
        Address address = get(profileId, addressId, addressRepository);
        BeanUtils.copyProperties(command, address);
        return addressRepository.save(address);
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

    public static void delete(Long profileId, Long addressId, AddressRepository addressRepository) {
        // check if address owned by requester
        get(profileId, addressId, addressRepository);
        addressRepository.deleteById(addressId);
    }

    private boolean isDuplicateOf(CreateAddressCommand command) {
        return Objects.equals(fullName, command.getFullName()) &&
                Objects.equals(line1, command.getLine1()) &&
                Objects.equals(line2, command.getLine2()) &&
                Objects.equals(postalCode, command.getPostalCode()) &&
                Objects.equals(phoneNumber, command.getPhoneNumber()) &&
                Objects.equals(city, command.getCity()) &&
                Objects.equals(province, command.getProvince()) &&
                Objects.equals(country, command.getCountry());
    }
}
