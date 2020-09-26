package com.hw.aggregate.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizOrderAddress implements Serializable {

    @NotBlank
    @Column(nullable = false)
    private String orderAddressFullName;

    @NotBlank
    @Column(nullable = false)
    private String orderAddressLine1;

    @Column
    private String orderAddressLine2;

    @NotBlank
    @Column(nullable = false)
    private String orderAddressPostalCode;

    @NotBlank
    @Column(nullable = false)
    private String orderAddressPhoneNumber;

    @NotBlank
    @Column(nullable = false)
    private String orderAddressCity;

    @NotBlank
    @Column(nullable = false)
    private String orderAddressProvince;

    @NotBlank
    @Column(nullable = false)
    private String orderAddressCountry;

}
