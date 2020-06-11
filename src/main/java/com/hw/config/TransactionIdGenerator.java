package com.hw.config;

import java.util.UUID;

public class TransactionIdGenerator {
    public static String getId() {
        return UUID.randomUUID().toString();
    }
}
