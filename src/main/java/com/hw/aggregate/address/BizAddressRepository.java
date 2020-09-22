package com.hw.aggregate.address;

import com.hw.aggregate.address.model.BizAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BizAddressRepository extends JpaRepository<BizAddress, Long> {
}
