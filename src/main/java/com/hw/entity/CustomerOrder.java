package com.hw.entity;

import com.hw.clazz.MapConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "CustomerOrder")
@SequenceGenerator(name = "orderId_gen", sequenceName = "orderId_gen", initialValue = 100)
@Data
public class CustomerOrder extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "orderId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @NotEmpty
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_order", insertable = false, updatable = false)
    private Address address;

    @NotNull
    @NotEmpty
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_order", insertable = false, updatable = false)
    private Payment payment;

    @Column
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_order")
    private List<Product> productList;


    @Column
    @Convert(converter = MapConverter.class)
    private Map<String,String> additionalFees;

    @Column
    @NotNull
    private BigDecimal finalPrice;

    @Column
    @NotNull
    private BigDecimal totalProductPrice;

}

