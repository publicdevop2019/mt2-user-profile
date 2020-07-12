package com.hw.aggregate.order.model;

import com.hw.shared.EnumDBConverter;

public enum TaskStatus {
    STARTED,
    ROLLBACK,
    COMPLETED;

    public static class DBConverter extends EnumDBConverter {
        public DBConverter() {
            super(TaskStatus.class);
        }
    }
}
