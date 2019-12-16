package com.hw.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "Payment")
@SequenceGenerator(name = "paymentId_gen", sequenceName = "paymentId_gen", initialValue = 100)
@Data
public class Payment extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "paymentId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

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
        Payment payment = (Payment) o;
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
