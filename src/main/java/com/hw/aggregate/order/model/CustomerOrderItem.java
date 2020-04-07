package com.hw.aggregate.order.model;

import com.hw.clazz.ProductOptionMapper;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Embeddable
@Data
public class CustomerOrderItem {

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String name;

    @Column(length = 10000)
    @Convert(converter = ProductOptionMapper.class)
    private List<CustomerOrderItemAddOn> selectedOptions;

    @NotNull
    @Column
    private String finalPrice;

    @Column
    private String imageUrlSmall;

    @NotNull
    @Column
    private String productId;

    @ManyToOne
    private CustomerOrder customerOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerOrderItem product = (CustomerOrderItem) o;
        return Objects.equals(name, product.name) &&
                /**
                 * use deepEquals for JPA persistentBag workaround, otherwise equals will return incorrect result
                 */
                Objects.deepEquals(selectedOptions != null ? selectedOptions.toArray() : new Object[0], product.selectedOptions != null ? product.selectedOptions.toArray() : new Object[0]) &&
                Objects.equals(finalPrice, product.finalPrice) &&
                Objects.equals(imageUrlSmall, product.imageUrlSmall) &&
                Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, selectedOptions, finalPrice, imageUrlSmall, productId);
    }
}
