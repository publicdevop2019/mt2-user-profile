package com.hw.aggregate.address;

import com.hw.aggregate.address.command.AddAddressCommand;
import com.hw.aggregate.address.command.DeleteAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.exception.AddressNotExistException;
import com.hw.aggregate.address.exception.DuplicateAddressException;
import com.hw.aggregate.address.exception.MaxAddressCountException;
import com.hw.aggregate.address.model.Address;
import com.hw.aggregate.address.representation.AddressRepresentation;
import com.hw.aggregate.address.representation.AddressSummaryRepresentation;
import com.hw.aggregate.profile.ProfileRepo;
import com.hw.aggregate.profile.model.Profile;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AddressApplicationService {

    @Autowired
    private ProfileRepo profileRepo;

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public AddressSummaryRepresentation getAllAddresses(String authUserId, Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        return new AddressSummaryRepresentation(profileByResourceOwnerId.get().getAddressList());
    }

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public AddressRepresentation getAddressInById(String authUserId, Long profileId, Long addressId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<Address> collect = findById.get().getAddressList().stream().filter(e -> e.getId().equals(addressId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new AddressNotExistException();
        return new AddressRepresentation(collect.get(0));
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public AddressRepresentation createAddress(String authUserId, Long profileId, AddAddressCommand addAddressCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty() || findById.get().getAddressList().stream().anyMatch(e -> e.equals(addAddressCommand))) {
            log.info("same address found");
            throw new DuplicateAddressException();
        }
        if (findById.get().getAddressList().size() == 5)
            throw new MaxAddressCountException();
        findById.get().getAddressList().add((addAddressCommand));
        Profile save = profileRepo.save(findById.get());
        return new AddressRepresentation(save.getAddressList().stream().filter(e -> e.equals(addAddressCommand)).findFirst().get());
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void updateAddress(String authUserId, Long profileId, Long addressId, UpdateAddressCommand updateAddressCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<Address> collect = findById.get().getAddressList().stream().filter(e -> e.getId().equals(addressId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new AddressNotExistException();
        Address oldAddress = collect.get(0);
        BeanUtils.copyProperties(updateAddressCommand, oldAddress);
        profileRepo.save(findById.get());
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteAddress(String authUserId, Long profileId, DeleteAddressCommand deleteAddressCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<Address> collect = findById.get().getAddressList().stream().filter(e -> e.getId().equals(deleteAddressCommand.addressId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new AddressNotExistException();
        Address toBeRemoved = collect.get(0);
        findById.get().getAddressList().removeIf(e -> e.getId().equals(toBeRemoved.getId()));
        profileRepo.save(findById.get());
    }

}
