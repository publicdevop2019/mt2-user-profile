package com.hw.repo;

import com.hw.entity.OrderDetail;
import com.hw.entity.Profile;

import java.util.Map;

public interface OrderService {
    public String reserveOrder(OrderDetail orderDetail, Profile profile) throws RuntimeException;

    public Boolean confirmOrder(String profileId,String orderId) throws RuntimeException;

    public String replaceOrder(OrderDetail orderDetail, long orderId, long profileId);

    public Profile updateOrderById(String profileId, String orderId, OrderDetail orderDetail) throws RuntimeException;

    public void decreaseStorage(Map<String, Integer> productMap);

    public void increaseStorage(Map<String, Integer> productMap);

    public void decreaseActualStorage(Map<String, Integer> productMap);

    public void notifyBusinessOwner(Map<String, String> contentMap);

    public void releaseExpiredOrder();

}
