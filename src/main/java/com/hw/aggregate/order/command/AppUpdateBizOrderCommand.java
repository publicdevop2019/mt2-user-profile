package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

@Data
public class AppUpdateBizOrderCommand {
    private long orderId;
    private Boolean paymentStatus;
    private BizOrderStatus orderState;
}
