package com.hw.aggregate.order.model.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AppProductSku {
    private Set<String> attributesSales;
    private BigDecimal price;
}
