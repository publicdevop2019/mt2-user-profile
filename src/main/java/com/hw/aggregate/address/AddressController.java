package com.hw.aggregate.address;

import com.hw.aggregate.address.command.CreateAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.representation.AddressRepresentation;
import com.hw.aggregate.address.representation.AddressSummaryRepresentation;
import com.hw.shared.ServiceUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class AddressController {

    @Autowired
    private AddressApplicationService addressApplicationService;

    @GetMapping("profiles/{profileId}/addresses")
    public ResponseEntity<List<AddressSummaryRepresentation.AddressSummaryCustomerRepresentation>> getAllAddresses(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(addressApplicationService.getAllAddressesForCustomer(ServiceUtility.getUserId(authorization), profileId).getAddressList());
    }

    @GetMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<AddressRepresentation> getAddressInById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId) {
        return ResponseEntity.ok(addressApplicationService.getAddressInById(ServiceUtility.getUserId(authorization), profileId, addressId));
    }

    @PostMapping("profiles/{profileId}/addresses")
    public ResponseEntity<Void> createAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody CreateAddressCommand address) {
        return ResponseEntity.ok().header("Location", addressApplicationService.createAddress(ServiceUtility.getUserId(authorization), profileId, address).getId().toString()).build();
    }

    @PutMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<Void> updateAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId, @RequestBody UpdateAddressCommand newAddress) {
        addressApplicationService.updateAddress(ServiceUtility.getUserId(authorization), profileId, addressId, newAddress);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("profiles/{profileId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "addressId") Long addressId) {
        addressApplicationService.deleteAddress(ServiceUtility.getUserId(authorization), profileId, addressId);
        return ResponseEntity.ok().build();
    }

}
