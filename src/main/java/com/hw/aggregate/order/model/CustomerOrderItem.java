package com.hw.aggregate.order.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Embeddable
@Data
public class CustomerOrderItem implements Serializable {

    private static final long serialVersionUID = 1;

    @ManyToOne
    private transient CustomerOrder customerOrder;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 10000)
    @Convert(converter = ProductOptionMapper.class)
    private List<CustomerOrderItemAddOn> selectedOptions;

    @NotBlank
    @Column(nullable = false)
    private String finalPrice;

    @NotBlank
    @Column(nullable = false)
    private String productId;

    private String imageUrlSmall;

}
