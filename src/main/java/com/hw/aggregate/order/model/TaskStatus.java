package com.hw.aggregate.order.model;

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
