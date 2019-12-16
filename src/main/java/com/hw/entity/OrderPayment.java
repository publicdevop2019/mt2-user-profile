package com.hw.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Embeddable
@Data
public class OrderPayment {

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String accountNumber;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String accountHolderName;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String expireDate;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String cvv;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderPayment payment = (OrderPayment) o;
        return Objects.equals(accountNumber, payment.accountNumber) &&
                Objects.equals(accountHolderName, payment.accountHolderName) &&
                Objects.equals(expireDate, payment.expireDate) &&
                Objects.equals(cvv, payment.cvv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, accountHolderName, expireDate, cvv);
    }
}
