package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;

public enum OrderState {
    NOT_PAID_RESERVED,
    NOT_PAID_RECYCLED,
    PAID_RESERVED,
    PAID_RECYCLED,
    CONFIRMED,
    DRAFT,
    ;

    public static class DBConverter implements AttributeConverter<OrderState, String> {
        @Override
        public String convertToDatabaseColumn(OrderState orderState) {
            return orderState.name();
        }

        @Override
        public OrderState convertToEntityAttribute(String s) {
            return OrderState.valueOf(s);
        }
    }
}
