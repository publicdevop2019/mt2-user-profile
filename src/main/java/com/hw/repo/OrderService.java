package com.hw.repo;

import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;
import com.hw.exceptions.OrderValidationException;

import java.util.Map;

public interface OrderService {
    public String reserveOrder(OrderDetail orderDetail, Profile profile) throws OrderValidationException;

    public Boolean confirmOrder(String orderId) throws OrderValidationException;

    public String replaceOrder(OrderDetail orderDetail, long orderId, long profileId);

    public Profile updateOrderById(String profileId, String orderId, OrderDetail orderDetail) throws OrderValidationException;

    public void decreaseStorage(Map<String, Integer> productMap);

    public void increaseStorage(Map<String, Integer> productMap);

    public void notifyBusinessOwner(Map<String, String> contentMap);

    public void releaseExpiredOrder();

}
