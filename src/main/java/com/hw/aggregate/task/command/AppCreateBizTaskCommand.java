package com.hw.aggregate.task.command;

import com.hw.aggregate.order.model.BizOrderEvent;
import lombok.Data;

@Data
public class AppCreateBizTaskCommand {
    private BizOrderEvent taskName;
    private String transactionId;
    private Long referenceId;
}
