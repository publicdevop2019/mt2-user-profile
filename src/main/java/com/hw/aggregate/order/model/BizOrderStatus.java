package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;

public enum BizOrderStatus {
    NOT_PAID_RESERVED,
    NOT_PAID_RECYCLED,
    PAID_RESERVED,
    PAID_RECYCLED,
    CONFIRMED,
    DRAFT,
    ;

    public static class DBConverter implements AttributeConverter<BizOrderStatus, String> {
        @Override
        public String convertToDatabaseColumn(BizOrderStatus orderState) {
            return orderState.name();
        }

        @Override
        public BizOrderStatus convertToEntityAttribute(String s) {
            return BizOrderStatus.valueOf(s);
        }
    }
}
