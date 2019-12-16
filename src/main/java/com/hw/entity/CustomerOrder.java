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
@Table(name = "CustomerOrder")
@SequenceGenerator(name = "orderId_gen", sequenceName = "orderId_gen", initialValue = 100)
@Data
public class CustomerOrder extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "orderId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Valid
    @Embedded
    private OrderAddress address;

    @NotNull
    @Valid
    @Embedded
    private OrderPayment payment;

    @Column(length = 10000)
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_order")
    private List<Product> productList;


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
        CustomerOrder that = (CustomerOrder) o;
        return
                Objects.equals(address, that.address) &&
                        /**
                         * use deepEquals for JPA persistentBag workaround
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
        return Objects.hash(address, productList, additionalFees, taxCost, shippingCost, finalPrice, totalProductPrice);
    }
}

