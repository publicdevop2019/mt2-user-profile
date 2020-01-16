package com.hw.repo;

import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;
import com.hw.exceptions.OrderValidationException;

import java.util.Map;

public interface OrderService {
    public String placeOrder(OrderDetail orderDetail, Profile profile) throws OrderValidationException;

    public void decreaseStorage(Map<String, Integer> productMap);

    public void increaseStorage(Map<String, Integer> productMap);

    public void notifyBusinessOwner(Map<String, String> contentMap);

}
