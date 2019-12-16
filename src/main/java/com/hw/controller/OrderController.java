package com.hw.controller;

import com.hw.clazz.OwnerOnly;
import com.hw.entity.CustomerOrder;
import com.hw.entity.Profile;
import com.hw.repo.ProfileRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "v1/api",produces = "application/json")
public class OrderController {

    @Autowired
    ProfileRepo profileRepo;
    @OwnerOnly
    @GetMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> getAllOrders(@RequestHeader("authorization") String authorization,@PathVariable(name="profileId") Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        if (profileByResourceOwnerId.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profileByResourceOwnerId.get().getOrderList());
    }
    @OwnerOnly
    @GetMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> getPaymentById(@RequestHeader("authorization") String authorization,@PathVariable(name = "profileId") Long profileId,@PathVariable(name = "orderId") Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.notFound().build();
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if(collect.size() !=1 )
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(collect.get(0));
    }
    @OwnerOnly
    @PostMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> createOrder(@RequestHeader("authorization") String authorization,@PathVariable(name = "profileId") Long profileId, @RequestBody CustomerOrder newOrder) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty() || findById.get().getOrderList().stream().anyMatch(e -> e.equals(newOrder)))
            return ResponseEntity.badRequest().build();
        if (findById.get().getOrderList() == null)
            findById.get().setOrderList(new ArrayList<>());
        findById.get().getOrderList().add(newOrder);
        Profile save = profileRepo.save(findById.get());
        return ResponseEntity.ok().header("Location", save.getOrderList().stream().filter(e -> e.equals(newOrder)).findFirst().get().getId().toString()).build();
    }
    @OwnerOnly
    @PutMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> updateOrder(@RequestHeader("authorization") String authorization,@PathVariable(name = "profileId") Long profileId,@PathVariable(name = "orderId") Long orderId, @RequestBody CustomerOrder newOrder) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if(collect.size() !=1 )
            return ResponseEntity.badRequest().build();

        CustomerOrder oldOrder = collect.get(0);
        BeanUtils.copyProperties(newOrder, oldOrder);
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }
    @OwnerOnly
    @DeleteMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("authorization") String authorization,@PathVariable(name = "profileId") Long profileId,@PathVariable(name = "orderId") Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<CustomerOrder> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if(collect.size() !=1 )
            return ResponseEntity.badRequest().build();

        CustomerOrder toBeRemoved = collect.get(0);
        findById.get().getOrderList().removeIf(e->e.getId().equals(toBeRemoved.getId()));
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }
}
