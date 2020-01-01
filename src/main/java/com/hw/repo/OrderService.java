package com.hw.repo;

import java.util.Map;

public interface OrderService {
    public void deductAmount(Map<String,Integer> productMap);
}
