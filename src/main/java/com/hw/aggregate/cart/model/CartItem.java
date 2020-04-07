package com.hw.aggregate.cart.model;

import com.hw.aggregate.order.model.CustomerOrderItemAddOn;
import com.hw.clazz.ProductOptionMapper;
import com.hw.aggregate.order.model.CustomerOrder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Cart")
@SequenceGenerator(name = "cartId_gen", sequenceName = "cartId_gen", initialValue = 100)
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cartId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

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
        CartItem product = (CartItem) o;
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
