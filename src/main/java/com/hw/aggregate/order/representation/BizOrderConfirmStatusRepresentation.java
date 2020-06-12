package com.hw.aggregate.order.representation;

import lombok.Data;

@Data
public class BizOrderConfirmStatusRepresentation {
    private Boolean paymentStatus;

    public BizOrderConfirmStatusRepresentation(Boolean paid) {
        this.paymentStatus = paid;
    }
}
