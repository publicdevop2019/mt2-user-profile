package com.hw.controller;

import com.hw.clazz.OwnerOnly;
import com.hw.clazz.ProductOption;
import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;
import com.hw.repo.OrderService;
import com.hw.repo.ProfileRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "v1/api", produces = "application/json")
public class OrderController {

    @Autowired
    ProfileRepo profileRepo;

    @Autowired
    OrderService orderService;

    @OwnerOnly
    @GetMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> getAllOrders(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        if (profileByResourceOwnerId.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profileByResourceOwnerId.get().getOrderList());
    }

    @OwnerOnly
    @GetMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> getOrderById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.notFound().build();
        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(collect.get(0));
    }

    @OwnerOnly
    @PostMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> createOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody OrderDetail newOrder) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        if (findById.get().getOrderList() == null)
            findById.get().setOrderList(new ArrayList<>());
        List<OrderDetail> orderList = findById.get().getOrderList();
        int beforeInsert = orderList.size();
        orderList.add(newOrder);
        /**
         * deduct product storage, this is a performance bottleneck with sync http
         */
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        newOrder.getProductList().forEach(e -> {
            int defaultAmount = 1;
            if (e.getSelectedOptions() != null) {
                Optional<ProductOption> qty = e.getSelectedOptions().stream().filter(el -> el.title.equals("qty")).findFirst();
                if (qty.isPresent() && !qty.get().options.isEmpty()) {
                    /**
                     * deduct amount based on qty value, otherwise default is 1
                     */
                    defaultAmount = Integer.parseInt(qty.get().options.get(0).optionValue);
                }
            }
            if (stringIntegerHashMap.containsKey(e.getProductId())) {
                stringIntegerHashMap.put(e.getProductId(), stringIntegerHashMap.get(e.getProductId()) + defaultAmount);
            } else {
                stringIntegerHashMap.put(e.getProductId(), defaultAmount);
            }
        });
        orderService.deductAmount(stringIntegerHashMap);
        Profile save = profileRepo.save(findById.get());
        return ResponseEntity.ok().header("Location", save.getOrderList().get(beforeInsert).getId().toString()).build();
    }

    @OwnerOnly
    @PutMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> updateOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId, @RequestBody OrderDetail newOrder) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();

        OrderDetail oldOrder = collect.get(0);
        BeanUtils.copyProperties(newOrder, oldOrder);
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }

    @OwnerOnly
    @DeleteMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.isEmpty())
            return ResponseEntity.badRequest().build();
        List<OrderDetail> collect = findById.get().getOrderList().stream().filter(e -> e.getId().equals(orderId)).collect(Collectors.toList());
        if (collect.size() != 1)
            return ResponseEntity.badRequest().build();

        OrderDetail toBeRemoved = collect.get(0);
        findById.get().getOrderList().removeIf(e -> e.getId().equals(toBeRemoved.getId()));
        profileRepo.save(findById.get());
        return ResponseEntity.ok().build();
    }
}
