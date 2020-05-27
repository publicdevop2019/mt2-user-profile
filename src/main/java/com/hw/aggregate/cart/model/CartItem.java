package com.hw.aggregate.cart.model;

import com.hw.aggregate.order.model.CustomerOrderItemAddOn;
import com.hw.clazz.ProductOptionMapper;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Cart")
@Data
public class CartItem {

    @Id
    private Long id;

    @Column(name = "fk_profile")
    private Long profileId;

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

    public CartItem() {

    }

    public static CartItem create(Long id, Long profileId, String name, List<CustomerOrderItemAddOn> selectedOptions, String finalPrice, String imageUrlSmall, String productId) {
        return new CartItem(id, profileId, name, selectedOptions, finalPrice, imageUrlSmall, productId);
    }

    private CartItem(Long id, Long profileId, String name, List<CustomerOrderItemAddOn> selectedOptions, String finalPrice, String imageUrlSmall, String productId) {
        this.id = id;
        this.name = name;
        this.selectedOptions = selectedOptions;
        this.finalPrice = finalPrice;
        this.imageUrlSmall = imageUrlSmall;
        this.productId = productId;
        this.productId = productId;
        this.profileId = profileId;
    }

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
                Objects.equals(profileId, product.profileId) &&
                Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, selectedOptions, finalPrice, imageUrlSmall, productId, profileId);
    }
}
