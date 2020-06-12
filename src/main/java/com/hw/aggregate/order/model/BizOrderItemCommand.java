package com.hw.aggregate.order.model;

import lombok.Data;

import java.util.List;

@Data
public class BizOrderItemCommand {
    private String name;
    private List<BizOrderItemAddOnCommand> selectedOptions;
    private String finalPrice;
    private String productId;
    private String imageUrlSmall;

}
