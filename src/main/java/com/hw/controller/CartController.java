package com.hw.controller;

import com.hw.clazz.OwnerOnly;
import com.hw.entity.Product;
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
@RequestMapping(path = "v1/api", produces = "application/json")
public class CartController {
    @Autowired
    ProfileRepo profileRepo;

    @OwnerOnly
    @GetMapping("profiles/{profileId}/cart")
    public ResponseEntity<?> getCartItems(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        if (profileByResourceOwnerId.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profileByResourceOwnerId.get().getCartList());
    }

    @OwnerOnly
    @PostMapping("profiles/{profileId}/cart")
    public ResponseEntity<?> addCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody Product newCartItem) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        /**
         * add timestamp to cart item to make it time sensitive, user can add same item multiple times
         */
//        if (findById.isEmpty() || findById.get().getCartList().stream().anyMatch(e -> e.equals(newCartItem)))
//            return ResponseEntity.badRequest().build();
        if (findById.get().getCartList() == null)
            findById.get().setCartList(new ArrayList<>());
        findById.get().getCartList().add(newCartItem);
        Profile save = profileRepo.save(findById.get());
        return ResponseEntity.ok().header("Location", save.getCartList().stream().filter(e -> e.equals(newCartItem)).findFirst().get().getId().toString()).build();
    }

    @OwnerOnly
    @PutMapping("profiles/{profileId}/cart/{cartItemId}")
    public ResponseEntity<?> updateCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "cartItemId") Long cartItemId, @RequestBody Product updatedCartItem) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<Product> collect = findById.get().getCartList().stream().filter(e -> e.getId().equals(cartItemId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        Product oldCartItem = collect.get(0);
        BeanUtils.copyProperties(updatedCartItem, oldCartItem);
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }

    @OwnerOnly
    @DeleteMapping("profiles/{profileId}/cart/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "cartItemId") Long cartItemId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<Product> collect = findById.get().getCartList().stream().filter(e -> e.getId().equals(cartItemId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        Product tobeRemoved = collect.get(0);
        findById.get().getCartList().removeIf(e -> e.getId().equals(tobeRemoved.getId()));
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }
}
