package com.hw.controller;

import com.hw.clazz.PaymentStatus;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;
import com.hw.exceptions.OrderValidationException;
import com.hw.repo.OrderService;
import com.hw.repo.ProfileRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "v1/api", produces = "application/json")
public class OrderController {

    @Autowired
    ProfileRepo profileRepo;

    @Autowired
    OrderService orderService;

    @GetMapping("orders")
    public ResponseEntity<?> getAllOrdersForAdmin(@RequestHeader("authorization") String authorization) {
        List<OrderDetail> collect = profileRepo.findAll().stream().map(Profile::getOrderList).flatMap(Collection::stream).collect(Collectors.toList());
        return ResponseEntity.ok(collect);
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> getAllOrders(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        return ResponseEntity.ok(profileByResourceOwnerId.get().getOrderList());
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> getOrderById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(collect.get(0));
    }

    @ProfileExistAndOwnerOnly
    @PostMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> reserveOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody OrderDetail newOrder) {
        String paymentLink;
        Optional<Profile> findById = profileRepo.findById(profileId);
        try {
            paymentLink = orderService.reserveOrder(newOrder, findById.get());
        } catch (OrderValidationException ex) {
            log.error("unable to reserve order ", ex);
            return ResponseEntity.badRequest().build();
        }
        /**
         * clear shopping cart
         */
        findById.get().getCartList().clear();
        profileRepo.save(findById.get());
        return ResponseEntity.ok().header("Location", paymentLink).build();
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        OrderDetail orderDetail = collect.get(0);
        HashMap<String, Boolean> stringStringHashMap = new HashMap<>();

        if (orderService.confirmOrder(orderId.toString())) {
            orderDetail.setPaymentStatus(PaymentStatus.paid);
            stringStringHashMap.put("paymentStatus", Boolean.TRUE);
        } else {
            orderDetail.setPaymentStatus(PaymentStatus.unpaid);
            stringStringHashMap.put("paymentStatus", Boolean.FALSE);
        }
        profileRepo.save(findById.get());
        try {
            Map<String, String> contentMap = new HashMap<>();
            /**
             * @todo add order details
             */
            orderService.notifyBusinessOwner(contentMap);
        } catch (Exception ex) {
            log.error("unable to notify business owner", ex);
        }
        return ResponseEntity.ok(stringStringHashMap);
    }

    @ProfileExistAndOwnerOnly
    @PutMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> updateOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId, @RequestBody OrderDetail newOrder) {
        try {
            orderService.updateOrderById(profileId.toString(), orderId.toString(), newOrder);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @ProfileExistAndOwnerOnly
    @DeleteMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        OrderDetail toBeRemoved = collect.get(0);
        findById.get().getOrderList().removeIf(e -> e.getId().equals(toBeRemoved.getId()));
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }
}
