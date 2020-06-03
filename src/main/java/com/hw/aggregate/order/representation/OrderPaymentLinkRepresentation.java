package com.hw.aggregate.order.representation;

import lombok.Data;

@Data
public class OrderPaymentLinkRepresentation {
    private String paymentLink;
    private Boolean paymentState;

    public OrderPaymentLinkRepresentation(String paymentLink, boolean paymentState) {
        this.paymentLink = paymentLink;
        this.paymentState = paymentState;
    }
}
