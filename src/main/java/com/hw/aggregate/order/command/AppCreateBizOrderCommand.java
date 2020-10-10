package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AppCreateBizOrderCommand implements Serializable {
    private static final long serialVersionUID = 1;
    private long orderId;
    private long userId;
    private BizOrderStatus orderState;
    private String createdBy;
    private BizOrderAddressCmdRep address;
    private List<BizOrderItem> productList;
    private String paymentType;
    private BigDecimal paymentAmt;
    private String paymentLink;
}
