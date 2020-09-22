package com.hw.aggregate.task.model;

import com.hw.aggregate.order.model.BizOrderEvent;
import com.hw.aggregate.task.command.AppCreateBizTaskCommand;
import com.hw.aggregate.task.command.AppUpdateBizTaskCommand;
import com.hw.shared.Auditable;
import com.hw.shared.rest.IdBasedEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@Data
@NoArgsConstructor
public class BizTask extends Auditable implements IdBasedEntity {
    @Id
    private Long id;

    @Column(length = 25)
    @Convert(converter = BizOrderEvent.DBConverter.class)
    private BizOrderEvent taskName;
    public static final String ENTITY_TASK_NAME = "taskName";

    @Column(length = 25)
    @Convert(converter = BizTaskStatus.DBConverter.class)
    private BizTaskStatus taskStatus;
    public static final String ENTITY_TASK_STATUS = "taskStatus";

    private String transactionId;
    private String rollbackReason;
    private Long referenceId;
    public static final String ENTITY_REFERENCE_ID = "referenceId";

    @Version
    private Integer version;

    public static BizTask create(Long id, AppCreateBizTaskCommand command) {
        return new BizTask(id, command);
    }

    public BizTask(Long id, AppCreateBizTaskCommand command) {
        this.id = id;
        this.taskName = command.getTaskName();
        this.taskStatus = BizTaskStatus.STARTED;
        this.transactionId = command.getTransactionId();
        this.referenceId = command.getReferenceId();
    }

    public BizTask replace(AppUpdateBizTaskCommand command) {
        this.setTaskStatus(command.getTaskStatus());
        this.setRollbackReason(command.getRollbackReason());
        return this;
    }
}
