package com.hw.aggregate.order;

import com.hw.aggregate.order.model.BizOrderEvent;
import com.hw.aggregate.order.model.BizOrderStatus;
import com.hw.shared.sql.PatchCommand;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SagaOrchestratorService {

    public BizStateMachineRep start(CreateBizStateMachineCommand createBizStateMachineCommand) {
        return null;
    }

    @Data
    public static class CreateBizStateMachineCommand {
        private long orderId;
        private long userId;
        private String txId;
        private BizOrderStatus orderState;
        private BizOrderEvent bizOrderEvent;
        private BizOrderEvent prepareEvent;
        private String createdBy;
        private List<PatchCommand> orderStorageChange;
        private List<PatchCommand> actualStorageChange;
    }

    @Data
    public static class BizStateMachineRep {
        private String paymentLink;
        private BizOrderStatus orderState;
        private Boolean paymentStatus;
    }
}
