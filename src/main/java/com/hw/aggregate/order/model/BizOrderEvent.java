package com.hw.aggregate.order.model;

import com.hw.shared.EnumDBConverter;

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

    public static class DBConverter extends EnumDBConverter {
        public DBConverter() {
            super(BizOrderEvent.class);
        }
    }

}
