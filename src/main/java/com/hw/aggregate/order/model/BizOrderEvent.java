package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;

/**
 * use separate prepare event so logic will not miss triggered
 */
public enum BizOrderEvent {
    CONFIRM_PAYMENT,
    CONFIRM_ORDER,
    NEW_ORDER,
    PREPARE_CONFIRM_ORDER,
    PREPARE_NEW_ORDER,
    PREPARE_RESERVE,
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
