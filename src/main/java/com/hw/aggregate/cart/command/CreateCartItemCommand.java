package com.hw.aggregate.cart.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hw.aggregate.order.model.BizOrderItemAddOn;
import lombok.Data;

import java.util.LinkedHashSet;
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
    @JsonDeserialize(as= LinkedHashSet.class)//use linkedHashSet to keep order of elements as it is received
    private Set<String> attributesSales;

    private Map<String, String> attrIdMap;

}
