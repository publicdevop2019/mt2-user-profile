package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;

public enum BizOrderEvent {
    CONFIRM_PAYMENT,
    CONFIRM_ORDER,
    NEW_ORDER,
    PREPARE,
    RESERVE;

    public static class DBConverter implements AttributeConverter<BizOrderEvent, String> {
        @Override
        public String convertToDatabaseColumn(BizOrderEvent orderState) {
            return orderState.name();
        }

        @Override
        public BizOrderEvent convertToEntityAttribute(String s) {
            return BizOrderEvent.valueOf(s);
        }
    }
}
