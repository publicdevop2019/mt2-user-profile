package com.hw.aggregate.order.representation;

import lombok.Data;

@Data
public class BizOrderPaymentLinkRepresentation {
    private String paymentLink;
    private Boolean paymentStatus;

    public BizOrderPaymentLinkRepresentation(String paymentLink, boolean paymentStatus) {
        this.paymentLink = paymentLink;
        this.paymentStatus = paymentStatus;
    }
}
