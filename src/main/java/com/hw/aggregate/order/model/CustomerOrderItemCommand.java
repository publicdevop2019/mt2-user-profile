package com.hw.aggregate.order.model;

import lombok.Data;

import java.util.List;

@Data
public class CustomerOrderItemCommand {
    private String name;
    private List<CustomerOrderItemAddOnCommand> selectedOptions;
    private String finalPrice;
    private String productId;
    private String imageUrlSmall;

}
