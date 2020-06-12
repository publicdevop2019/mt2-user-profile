package com.hw.aggregate.profile.model;

import com.hw.aggregate.address.model.Address;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.profile.ProfileRepo;
import com.hw.aggregate.profile.exception.ProfileAlreadyExistException;
import com.hw.aggregate.profile.exception.ProfileNotExistException;
import com.hw.shared.Auditable;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "Profile")
@Data
@NoArgsConstructor
public class Profile extends Auditable {
    @Id
    private Long id;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<Address> addressList;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<BizOrder> orderList;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_profile")
    private List<CartItem> cartList;

    private Long resourceOwnerId;

    public static Profile get(String userId, ProfileRepo repo) {
        Optional<Profile> profileByResourceOwnerId = repo.findProfileByResourceOwnerId(Long.parseLong(userId));
        if (profileByResourceOwnerId.isEmpty())
            throw new ProfileNotExistException();
        return profileByResourceOwnerId.get();
    }

    public static Profile create(String userId, ProfileRepo repo, Long id) {
        Optional<Profile> profileByResourceOwnerId = repo.findProfileByResourceOwnerId(Long.parseLong(userId));
        if (profileByResourceOwnerId.isPresent())
            throw new ProfileAlreadyExistException();
        Profile profile = new Profile();
        profile.setId(id);
        profile.setResourceOwnerId(Long.parseLong(userId));
        return repo.save(profile);
    }
}
