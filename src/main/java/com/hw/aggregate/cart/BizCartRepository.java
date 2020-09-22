package com.hw.aggregate.cart;

import com.hw.aggregate.cart.model.BizCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BizCartRepository extends JpaRepository<BizCartItem, Long> {
}
