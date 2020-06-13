package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;

public enum TaskStatus {
    STARTED,
    ROLLBACK,
    COMMITTED;

    public static class DBConverter implements AttributeConverter<TaskStatus, String> {
        @Override
        public String convertToDatabaseColumn(TaskStatus orderState) {
            return orderState.name();
        }

        @Override
        public TaskStatus convertToEntityAttribute(String s) {
            return TaskStatus.valueOf(s);
        }
    }
}
