package com.hw.aggregate.order.command;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class BizOrderItemCommand implements Serializable {
    private static final long serialVersionUID = 1;
    private String name;
    private List<BizOrderItemAddOnCommand> selectedOptions;
    private BigDecimal finalPrice;
    private Long productId;
    private String imageUrlSmall;
    private Set<String> attributesSales;
    private Map<String,String> attrIdMap;

}
