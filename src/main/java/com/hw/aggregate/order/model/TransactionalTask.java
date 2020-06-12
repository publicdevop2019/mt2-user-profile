package com.hw.aggregate.order.model;

import com.hw.shared.Auditable;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
@Data
@NoArgsConstructor
public class TransactionalTask extends Auditable {
    private Long id;
    private OrderEvent taskName;
    private TaskStatus taskStatus;
    private String transactionId;
    private Long customerOrderId;

    public TransactionalTask(Long id, OrderEvent taskName, TaskStatus taskStatus, String transactionId, Long customerOrderId) {
        this.id = id;
        this.taskName = taskName;
        this.taskStatus = taskStatus;
        this.transactionId = transactionId;
        this.customerOrderId = customerOrderId;
    }
}
