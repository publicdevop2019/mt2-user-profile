package com.hw.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Profile")
@SequenceGenerator(name = "profileId_gen", sequenceName = "profileId_gen", initialValue = 100)
@Data
@NoArgsConstructor
public class Profile  extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "profileId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<Payment> paymentList;

    @Column
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<Address> addressList;

    @Column
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<CustomerOrder> orderList;

    @Column
    private Long resourceOwnerId;

}
