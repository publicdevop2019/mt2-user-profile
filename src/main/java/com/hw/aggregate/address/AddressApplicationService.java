package com.hw.aggregate.address;

import com.hw.aggregate.address.command.CreateAddressCommand;
import com.hw.aggregate.address.command.DeleteAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.exception.AddressNotExistException;
import com.hw.aggregate.address.exception.DuplicateAddressException;
import com.hw.aggregate.address.exception.MaxAddressCountException;
import com.hw.aggregate.address.model.Address;
import com.hw.aggregate.address.representation.AddressRepresentation;
import com.hw.aggregate.address.representation.AddressSummaryRepresentation;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AddressApplicationService {

    @Autowired
    private AddressRepository addressRepository;

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public AddressSummaryRepresentation getAllAddressesForCustomer(String authUserId, Long profileId) {
        List<Address> byProfileId = addressRepository.findByProfileId(profileId);
        return new AddressSummaryRepresentation(byProfileId);
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public AddressRepresentation getAddressInById(String authUserId, Long profileId, Long addressId) {
        Address addressForCustomer = getAddressForCustomer(profileId, addressId);
        return new AddressRepresentation(addressForCustomer);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public AddressRepresentation createAddress(String authUserId, Long profileId, CreateAddressCommand createAddressCommand) {
        List<Address> byProfileId = addressRepository.findByProfileId(profileId);
        if (byProfileId.size() == 5)
            throw new MaxAddressCountException();
        Address address = Address.create(
                profileId, createAddressCommand.getFullName(), createAddressCommand.getLine1(),
                createAddressCommand.getLine2(), createAddressCommand.getPostalCode(), createAddressCommand.getPhoneNumber(),
                createAddressCommand.getCity(), createAddressCommand.getProvince(), createAddressCommand.getCountry());
        if (byProfileId.stream().anyMatch(e -> e.equals(address))) {
            throw new DuplicateAddressException();
        }
        Address save = addressRepository.save(address);
        return new AddressRepresentation(save);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void updateAddress(String authUserId, Long profileId, Long addressId, UpdateAddressCommand updateAddressCommand) {
        Address addressForCustomer = getAddressForCustomer(profileId, addressId);
        BeanUtils.copyProperties(updateAddressCommand, addressForCustomer);
        addressRepository.save(addressForCustomer);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteAddress(String authUserId, Long profileId, DeleteAddressCommand deleteAddressCommand) {
        Address addressForCustomer = getAddressForCustomer(profileId, deleteAddressCommand.addressId);
        addressRepository.delete(addressForCustomer);
    }

    private Address getAddressForCustomer(Long profileId, Long addressId) {
        Optional<Address> byId = addressRepository.findById(addressId);
        if (byId.isEmpty())
            throw new AddressNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new AddressNotExistException();
        return byId.get();
    }

}
