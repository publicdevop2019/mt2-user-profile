package com.hw.aggregate.order.representation;

public class OrderPaymentLinkRepresentation {
    public String paymentLink;
    public Boolean paymentState;

    public OrderPaymentLinkRepresentation(String paymentLink, Boolean paymentState) {
        this.paymentLink = paymentLink;
        this.paymentState = paymentState;
    }
}
