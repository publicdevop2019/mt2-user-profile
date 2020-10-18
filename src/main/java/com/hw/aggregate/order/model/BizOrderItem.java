package com.hw.aggregate.order.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Embeddable
@Data
public class BizOrderItem implements Serializable {

    private static final long serialVersionUID = 1;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 10000)
    @Convert(converter = ProductOptionMapper.class)
    private List<BizOrderItemAddOn> selectedOptions;

    @NotBlank
    @Column(nullable = false)
    private BigDecimal finalPrice;

    @NotBlank
    @Column(nullable = false)
    private Long productId;
    @Convert(converter = StringSetConverter.class)
    private Set<String> attributesSales;

    private String imageUrlSmall;

    private HashMap<String,String> attrIdMap;
}
