package com.hw.aggregate.order.representation;

import lombok.Data;

@Data
public class OrderPaymentLinkRepresentation {
    private String paymentLink;
    private Boolean paymentStatus;

    public OrderPaymentLinkRepresentation(String paymentLink, boolean paymentStatus) {
        this.paymentLink = paymentLink;
        this.paymentStatus = paymentStatus;
    }
}
