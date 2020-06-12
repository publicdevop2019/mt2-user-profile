package com.hw.aggregate.order;

import com.hw.aggregate.order.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByProfileId(Long profileId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT p FROM #{#entityName} as p WHERE p.id = ?1")
    Optional<CustomerOrder> findByIdOptLock(Long id);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT p FROM #{#entityName} as p WHERE p.modifiedByUserAt < ?1 AND p.orderState = 'NOT_PAID_RESERVED'")
    List<CustomerOrder> findExpiredNotPaidReserved(Date expireAt);

    @Query("SELECT p FROM #{#entityName} as p WHERE p.orderState = 'PAID_RESERVED'")
    List<CustomerOrder> findPaidReserved();

    @Query("SELECT p FROM #{#entityName} as p WHERE p.orderState = 'DRAFT' AND p.modifiedByUserAt < ?1")
    List<CustomerOrder> findExpiredDraftOrders(Date expireAt);
}
