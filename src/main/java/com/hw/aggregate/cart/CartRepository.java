package com.hw.aggregate.cart;

import com.hw.aggregate.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByProfileId(Long profileId);
}
