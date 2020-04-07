package com.hw.aggregate.profile.model;

import com.hw.aggregate.address.model.Address;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.shared.Auditable;
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
public class Profile  extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "profileId_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<Address> addressList;

    @Column
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<CustomerOrder> orderList;

    @Column
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<CartItem> cartList;

    @Column
    private Long resourceOwnerId;

}
