package com.hw.aggregate.address;

import com.hw.aggregate.address.model.Address;
import com.hw.aggregate.order.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByProfileId(Long profileId);
}
