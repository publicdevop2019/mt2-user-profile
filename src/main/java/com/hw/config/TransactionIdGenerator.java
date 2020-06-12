package com.hw.config;

import java.util.UUID;

public class TransactionIdGenerator {
    public static String getTxId() {
        return UUID.randomUUID().toString();
    }
}
