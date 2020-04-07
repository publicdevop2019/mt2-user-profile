package com.hw.aggregate.address;

import com.hw.aggregate.address.command.AddAddressCommand;
import com.hw.aggregate.address.command.DeleteAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.representation.AddressRepresentation;
import com.hw.aggregate.address.representation.AddressSummaryRepresentation;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class AddressController {

    @Autowired
    private AddressApplicationService addressApplicationService;

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/addresses")
    public ResponseEntity<?> getAllAddresses(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        AddressSummaryRepresentation allAddresses = addressApplicationService.getAllAddresses(profileId);
        return ResponseEntity.ok(allAddresses.addressList);
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<?> getAddressInById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId) {
        AddressRepresentation addressInById = addressApplicationService.getAddressInById(profileId, addressId);
        return ResponseEntity.ok(addressInById.address);
    }

    @ProfileExistAndOwnerOnly
    @PostMapping("profiles/{profileId}/addresses")
    public ResponseEntity<?> createAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody AddAddressCommand address) {
        AddressRepresentation address1 = addressApplicationService.createAddress(profileId, address);
        return ResponseEntity.ok().header("Location", address1.address.getId().toString()).build();
    }

    @ProfileExistAndOwnerOnly
    @PutMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId, @RequestBody UpdateAddressCommand newAddress) {
        addressApplicationService.updateAddress(profileId, addressId, newAddress);
        return ResponseEntity.ok().build();
    }

    @ProfileExistAndOwnerOnly
    @DeleteMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId) {
        addressApplicationService.deleteAddress(profileId, new DeleteAddressCommand(addressId));
        return ResponseEntity.ok().build();
    }

}
