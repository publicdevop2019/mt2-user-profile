package com.hw.aggregate.order;

import com.hw.aggregate.order.model.TransactionalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionalTaskRepository extends JpaRepository<TransactionalTask, Long> {
}
