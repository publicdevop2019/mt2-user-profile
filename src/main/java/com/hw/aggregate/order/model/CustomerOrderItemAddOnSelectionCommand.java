package com.hw.aggregate.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerOrderItemAddOnSelectionCommand {

    private String optionValue;

    private String priceVar;
}
