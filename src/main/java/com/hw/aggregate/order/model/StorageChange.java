package com.hw.aggregate.order.model;

import lombok.Data;

import java.util.List;

@Data
public class StorageChange {
    private String txId;
    private List<StorageChangeDetail> changeList;
}
