package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;

public enum OrderStatus {
    NOT_PAID_RESERVED,
    NOT_PAID_RECYCLED,
    PAID_RESERVED,
    PAID_RECYCLED,
    CONFIRMED,
    DRAFT,
    DRAFT_CLEAN,
    ;

    public static class DBConverter implements AttributeConverter<OrderStatus, String> {
        @Override
        public String convertToDatabaseColumn(OrderStatus orderState) {
            return orderState.name();
        }

        @Override
        public OrderStatus convertToEntityAttribute(String s) {
            return OrderStatus.valueOf(s);
        }
    }
}
