package com.hw.aggregate.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerOrderItemAddOnSelection {
    public String optionValue;
    public String priceVar;
}
