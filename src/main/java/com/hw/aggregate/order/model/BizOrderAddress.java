package com.hw.aggregate.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizOrderAddress {

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
