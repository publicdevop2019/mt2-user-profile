package com.hw.aggregate.order.representation;

import lombok.Data;

@Data
public class BizOrderPaymentLinkRepresentation {
    private String paymentLink;

    public BizOrderPaymentLinkRepresentation(String paymentLink) {
        this.paymentLink = paymentLink;
    }
}
