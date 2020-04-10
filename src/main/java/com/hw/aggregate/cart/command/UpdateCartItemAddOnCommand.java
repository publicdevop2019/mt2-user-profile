package com.hw.aggregate.cart.command;

import com.hw.aggregate.order.model.CustomerOrderItemAddOn;

import java.util.List;

public class UpdateCartItemAddOnCommand {

    private String name;

    private List<CustomerOrderItemAddOn> selectedOptions;

    private String finalPrice;

    private String imageUrlSmall;

    private String productId;

}
