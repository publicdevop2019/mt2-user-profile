package com.hw.controller;

import com.hw.clazz.ProfileExistAndOwnerOnly;
import com.hw.entity.Payment;
import com.hw.entity.Profile;
import com.hw.repo.ProfileRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * note: authorization is reserved for swagger generator in order to have this field show up in swagger-ui
 * do not change param order bcz of aspectJ
 */
@RestController
@RequestMapping(path = "v1/api", produces = "application/json")
public class PaymentController {

    @Autowired
    ProfileRepo profileRepo;

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/payments")
    public ResponseEntity<?> getAllPayments(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        return ResponseEntity.ok(profileByResourceOwnerId.get().getPaymentList());
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/payments/{paymentId}")
    public ResponseEntity<?> getPaymentById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "paymentId") Long paymentId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<Payment> collect = findById.get().getPaymentList().stream().filter(e -> e.getId().equals(paymentId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(collect.get(0));
    }

    @ProfileExistAndOwnerOnly
    @PostMapping("profiles/{profileId}/payments")
    public ResponseEntity<?> createPayments(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody Payment payment) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty() || findById.get().getPaymentList().stream().anyMatch(e -> e.equals(payment)))
            return ResponseEntity.badRequest().build();
        findById.get().getPaymentList().add((payment));
        Profile save = profileRepo.save(findById.get());
        return ResponseEntity.ok().header("Location", save.getPaymentList().stream().filter(e -> e.equals(payment)).findFirst().get().getId().toString()).build();
    }

    @ProfileExistAndOwnerOnly
    @PutMapping("profiles/{profileId}/payments/{paymentId}")
    public ResponseEntity<?> updatePayments(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "paymentId") Long paymentId, @RequestBody Payment newPayment) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<Payment> collect = findById.get().getPaymentList().stream().filter(e -> e.getId().equals(paymentId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        Payment oldPayment = collect.get(0);
        BeanUtils.copyProperties(newPayment, oldPayment);
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }

    @ProfileExistAndOwnerOnly
    @DeleteMapping("profiles/{profileId}/payments/{paymentId}")
    public ResponseEntity<?> deletePayments(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "paymentId") Long paymentId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<Payment> collect = findById.get().getPaymentList().stream().filter(e -> e.getId().equals(paymentId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        Payment toBeRemoved = collect.get(0);
        findById.get().getPaymentList().removeIf(e -> e.getId().equals(toBeRemoved.getId()));
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }
}
