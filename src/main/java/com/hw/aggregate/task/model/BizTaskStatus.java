package com.hw.aggregate.task.model;

import com.hw.shared.EnumDBConverter;

public enum BizTaskStatus {
    STARTED,
    ROLLBACK,
    COMPLETED;

    public static class DBConverter extends EnumDBConverter {
        public DBConverter() {
            super(BizTaskStatus.class);
        }
    }
}
