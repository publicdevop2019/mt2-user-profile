package com.hw.aggregate.order.command;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class BizOrderItemAddOnSelectionCommand implements Serializable {
    private static final long serialVersionUID = 1;
    private String optionValue;

    private String priceVar;
}
