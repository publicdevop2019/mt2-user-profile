package com.hw.aggregate.order;

import com.hw.aggregate.order.model.TransactionalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionalTaskRepository extends JpaRepository<TransactionalTask, Long> {
    @Query("SELECT p FROM #{#entityName} as p WHERE p.createdAt < ?1 AND p.taskStatus = 'STARTED'")
    List<TransactionalTask> findExpiredStartedTasks(Date from);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT p FROM #{#entityName} as p WHERE p.id = ?1")
    Optional<TransactionalTask> findByIdOptLock(Long id);

}
