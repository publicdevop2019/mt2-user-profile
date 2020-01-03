package com.hw.entity;

import com.hw.clazz.MapConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "OrderDetail")
@SequenceGenerator(name = "orderId_gen", sequenceName = "orderId_gen", initialValue = 100)
@Data
public class OrderDetail extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "orderId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;
    /**
     * Address payment product all treat as embedded element instead of an entity
     */
    @NotNull
    @Valid
    @Embedded
    private SnapshotAddress address;

    @NotNull
    @Valid
    @Embedded
    private SnapshotPayment payment;

    @ElementCollection
    @CollectionTable(name = "order_product_snapshot", joinColumns = @JoinColumn(name = "order_id"))
    @Column
    private List<SnapshotProduct> productList;


    @Column
    @Convert(converter = MapConverter.class)
    private Map<String, String> additionalFees;

    @Column
    @NotNull
    private String taxCost;

    @Column
    @NotNull
    private String shippingCost;

    @Column
    @NotNull
    private String finalPrice;

    @Column
    @NotNull
    private String totalProductPrice;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetail that = (OrderDetail) o;
        return
                Objects.equals(id, that.id) &&
                        Objects.equals(address, that.address) &&
                        /**
                         * use deepEquals for JPA persistentBag workaround, otherwise equals will return incorrect result
                         */
                        Objects.deepEquals(productList.toArray(), that.productList.toArray()) &&
                        Objects.equals(additionalFees, that.additionalFees) &&
                        Objects.equals(taxCost, that.taxCost) &&
                        Objects.equals(shippingCost, that.shippingCost) &&
                        Objects.equals(finalPrice, that.finalPrice) &&
                        Objects.equals(totalProductPrice, that.totalProductPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, productList, additionalFees, taxCost, shippingCost, finalPrice, totalProductPrice);
    }
}

