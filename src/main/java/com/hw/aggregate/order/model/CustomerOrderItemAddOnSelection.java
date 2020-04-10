package com.hw.aggregate.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CustomerOrderItemAddOnSelection implements Serializable {

    private static final long serialVersionUID = 1;

    public String optionValue;

    public String priceVar;
}
