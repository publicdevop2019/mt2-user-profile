package com.hw.entity;

import com.hw.clazz.MapConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "Product")
@SequenceGenerator(name = "productId_gen", sequenceName = "productId_gen", initialValue = 100)
@Data
public class Product extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "productId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String name;

    @NotNull
    @NotEmpty
    @Column
    @Convert(converter = MapConverter.class)
    private Map<String,String> options;

    @NotNull
    @Column
    private BigDecimal finalPrice;

    @NotNull
    @Column
    private String imageUrl;

    @NotNull
    @Column
    private String referenceId;


}
