package com.hw.aggregate.order.model;

import com.hw.shared.Auditable;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@Data
@NoArgsConstructor
public class TransactionalTask extends Auditable {
    @Id
    private Long id;

    @Column(length = 25)
    @Convert(converter = BizOrderEvent.DBConverter.class)
    private BizOrderEvent taskName;

    @Column(length = 25)
    @Convert(converter = TaskStatus.DBConverter.class)
    private TaskStatus taskStatus;

    private String transactionId;
    private Long customerOrderId;

    @Version
    private Integer version;

    public TransactionalTask(Long id, BizOrderEvent taskName, TaskStatus taskStatus, String transactionId, Long customerOrderId) {
        this.id = id;
        this.taskName = taskName;
        this.taskStatus = taskStatus;
        this.transactionId = transactionId;
        this.customerOrderId = customerOrderId;
    }
}
