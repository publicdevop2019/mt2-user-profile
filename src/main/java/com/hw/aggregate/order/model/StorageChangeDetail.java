package com.hw.aggregate.order.model;

import lombok.Data;

import java.util.Set;

@Data
public class StorageChangeDetail {
    private Long productId;
    private Set<String> attributeSales;
    private Integer amount;
}
