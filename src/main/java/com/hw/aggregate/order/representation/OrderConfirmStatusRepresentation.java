package com.hw.aggregate.order.representation;

import lombok.Data;

@Data
public class OrderConfirmStatusRepresentation {
    private Boolean paymentStatus;

    public OrderConfirmStatusRepresentation(Boolean paid) {
        this.paymentStatus = paid;
    }
}
