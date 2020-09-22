package com.hw.aggregate.task.command;

import com.hw.aggregate.task.model.BizTaskStatus;
import lombok.Data;

@Data
public class AppUpdateBizTaskCommand {
    private BizTaskStatus taskStatus;
    private String rollbackReason;
}
