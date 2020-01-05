package com.hw.repo;

import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;

import java.util.Map;

public interface OrderService {
    public String placeOrder(OrderDetail orderDetail, Profile profile);

    public void decreaseStorage(Map<String, Integer> productMap);

    public void increaseStorage(Map<String, Integer> productMap);
}
