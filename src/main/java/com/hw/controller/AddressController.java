package com.hw.controller;

import com.hw.clazz.OwnerOnly;
import com.hw.entity.Address;
import com.hw.entity.Profile;
import com.hw.repo.ProfileRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "v1/api", produces = "application/json")
public class AddressController {

    @Autowired
    ProfileRepo profileRepo;

    @OwnerOnly
    @GetMapping("profiles/{profileId}/addresses")
    public ResponseEntity<?> getAllAddresses(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        if (profileByResourceOwnerId.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profileByResourceOwnerId.get().getAddressList());
    }

    @OwnerOnly
    @GetMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<?> getAddressInById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.notFound().build();
        List<Address> collect = findById.get().getAddressList().stream().filter(e -> e.getId().equals(addressId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(collect.get(0));
    }

    @OwnerOnly
    @PostMapping("profiles/{profileId}/addresses")
    public ResponseEntity<?> createAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody Address address) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty() || findById.get().getAddressList().stream().anyMatch(e -> e.equals(address)))
            return ResponseEntity.badRequest().build();
        findById.get().getAddressList().add((address));
        Profile save = profileRepo.save(findById.get());
        return ResponseEntity.ok().header("Location", save.getAddressList().stream().filter(e -> e.equals(address)).findFirst().get().getId().toString()).build();
    }

    @OwnerOnly
    @PutMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId, @RequestBody Address newAddress) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<Address> collect = findById.get().getAddressList().stream().filter(e -> e.getId().equals(addressId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        Address oldAddress = collect.get(0);
        BeanUtils.copyProperties(newAddress, oldAddress);
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }

    @OwnerOnly
    @DeleteMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<Address> collect = findById.get().getAddressList().stream().filter(e -> e.getId().equals(addressId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        Address toBeRemoved = collect.get(0);
        findById.get().getAddressList().removeIf(e -> e.getId().equals(toBeRemoved.getId()));
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }

}
