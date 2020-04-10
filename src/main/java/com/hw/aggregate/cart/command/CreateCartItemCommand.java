package com.hw.aggregate.cart.command;

import com.hw.aggregate.order.model.CustomerOrderItemAddOn;
import lombok.Data;

import java.util.List;

@Data
public class CreateCartItemCommand {

    private String name;

    private List<CustomerOrderItemAddOn> selectedOptions;

    private String finalPrice;

    private String imageUrlSmall;

    private String productId;

}
