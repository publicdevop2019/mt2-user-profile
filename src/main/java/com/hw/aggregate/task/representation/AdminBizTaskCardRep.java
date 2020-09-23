package com.hw.aggregate.task.representation;

import com.hw.aggregate.order.model.BizOrderEvent;
import com.hw.aggregate.task.model.BizTask;
import com.hw.aggregate.task.model.BizTaskStatus;
import lombok.Data;

import java.util.Date;
@Data
public class AdminBizTaskCardRep {
    private Long id;
    private BizOrderEvent taskName;
    private BizTaskStatus taskStatus;
    private String transactionId;
    private Long referenceId;
    private String rollbackReason;
    private Integer version;
    private String createdBy;
    private long createdAt;
    private String modifiedBy;
    private long modifiedAt;

    public AdminBizTaskCardRep(BizTask bizTask) {

        this.id = bizTask.getId();
        this.taskName = bizTask.getTaskName();
        this.taskStatus = bizTask.getTaskStatus();
        this.transactionId = bizTask.getTransactionId();
        this.rollbackReason = bizTask.getRollbackReason();
        this.referenceId = bizTask.getReferenceId();
        this.version = bizTask.getVersion();
        this.createdBy = bizTask.getCreatedBy();
        this.createdAt = bizTask.getCreatedAt().getTime();
        this.modifiedBy = bizTask.getModifiedBy();
        this.modifiedAt = bizTask.getModifiedAt().getTime();
    }
}
