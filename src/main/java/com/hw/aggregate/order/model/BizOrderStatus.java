package com.hw.aggregate.order.model;

public enum BizOrderStatus {
    NOT_PAID_RESERVED,
    NOT_PAID_RECYCLED,
    PAID_RESERVED,
    PAID_RECYCLED,
    CONFIRMED,
    DRAFT,
    ;

    public static class DBConverter extends EnumDBConverter {
        public DBConverter() {
            super(BizOrderStatus.class);
        }
    }
}
