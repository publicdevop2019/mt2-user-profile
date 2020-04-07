package com.hw.aggregate.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hw.shared.Auditable;
import lombok.Data;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "OrderDetail")
@SequenceGenerator(name = "orderId_gen", sequenceName = "orderId_gen", initialValue = 100)
@Data
public class CustomerOrder extends Auditable {
    /**
     * id setter is required to correctly work with BeanPropertyRowMapper for spring batch
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "orderId_gen")
    private Long id;
    /**
     * Address product all treat as embedded element instead of an entity
     */
    @NotNull
    @Valid
    @Embedded
    private CustomerOrderAddress address;


    @ElementCollection
    @CollectionTable(name = "order_product_snapshot", joinColumns = @JoinColumn(name = "order_id"))
    @Column
    private List<CustomerOrderItem> productList;

    @NotNull
    @NotEmpty
    private String paymentType;

    @Column
    @NotNull
    private BigDecimal paymentAmt;

    @Column
    private String paymentDate;

    @Column
    private CustomerOrderPaymentStatus paymentStatus;

    @Column
    @JsonIgnore
    private Date modifiedByUserAt;

    @Column
    private Boolean expired;

    @Column
    private Boolean revoked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerOrder that = (CustomerOrder) o;
        return
                Objects.equals(id, that.id) &&
                        Objects.equals(address, that.address) &&
                        /**
                         * use deepEquals for JPA persistentBag workaround, otherwise equals will return incorrect result
                         */
                        Objects.deepEquals(productList.toArray(), that.productList.toArray()) &&
                        Objects.equals(paymentType, that.paymentType) &&
                        Objects.equals(paymentAmt, that.paymentAmt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, productList, paymentType, paymentAmt);
    }
}

