package com.hw.entity;

import com.hw.clazz.ProductOption;
import com.hw.clazz.ProductOptionMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Product")
@SequenceGenerator(name = "productId_gen", sequenceName = "productId_gen", initialValue = 100)
@Data
public class Product extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "productId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String name;

    @Column(length = 10000)
    @Convert(converter = ProductOptionMapper.class)
    private List<ProductOption> selectedOptions;

    @NotNull
    @Column
    private String finalPrice;

    @Column
    private String imageUrlSmall;

    @NotNull
    @Column
    private String productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
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
