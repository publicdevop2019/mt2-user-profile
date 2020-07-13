package com.hw.aggregate.cart.command;

import com.hw.aggregate.order.model.BizOrderItemAddOn;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class CreateCartItemCommand {

    private String name;

    private List<BizOrderItemAddOn> selectedOptions;

    private String finalPrice;

    private String imageUrlSmall;

    private String productId;

    private Set<String> attributesSales;

    private Map<String, String> attrIdMap;

}
