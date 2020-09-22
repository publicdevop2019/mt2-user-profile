package com.hw.aggregate.task.representation;

import com.hw.aggregate.task.model.BizTask;
import lombok.Data;

@Data
public class AppBizTaskRep {

    private String transactionId;

    public AppBizTaskRep(BizTask bizTask) {
        this.transactionId = bizTask.getTransactionId();
    }

}
