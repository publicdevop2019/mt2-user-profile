package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.BizOrderStatus;
import com.hw.shared.rest.AggregateUpdateCommand;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppUpdateBizOrderCommand implements Serializable , AggregateUpdateCommand {
    private static final long serialVersionUID = 1;
    private long orderId;
    private Boolean paymentStatus;
    private BizOrderStatus orderState;
    private Integer version;
}
