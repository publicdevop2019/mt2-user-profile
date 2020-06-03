package com.hw.aggregate.address;

import com.hw.aggregate.address.command.CreateAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.model.Address;
import com.hw.aggregate.address.representation.AddressRepresentation;
import com.hw.aggregate.address.representation.AddressSummaryRepresentation;
import com.hw.config.ProfileExistAndOwnerOnly;
import com.hw.shared.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AddressApplicationService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private IdGenerator idGenerator;

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public AddressSummaryRepresentation getAllAddressesForCustomer(String authUserId, Long profileId) {
        return new AddressSummaryRepresentation(addressRepository.findByProfileId(profileId));
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public AddressRepresentation getAddressInById(String authUserId, Long profileId, Long addressId) {
        return new AddressRepresentation(Address.get(profileId, addressId, addressRepository));
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public AddressRepresentation createAddress(String authUserId, Long profileId, CreateAddressCommand createAddressCommand) {
        return new AddressRepresentation(Address.create(idGenerator.getId(), profileId, createAddressCommand, addressRepository));
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void updateAddress(String authUserId, Long profileId, Long addressId, UpdateAddressCommand command) {
        Address.update(profileId, addressId, addressRepository, command);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteAddress(String authUserId, Long profileId, Long addressId) {
        Address.delete(profileId, addressId, addressRepository);
    }
}
