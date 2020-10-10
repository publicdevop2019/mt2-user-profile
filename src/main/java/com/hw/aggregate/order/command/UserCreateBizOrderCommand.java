package com.hw.aggregate.order.command;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UserCreateBizOrderCommand implements Serializable {
    private static final long serialVersionUID = 1;
    private BizOrderAddressCmdRep address;
    private List<BizOrderItemCommand> productList;
    private String paymentType;
    private BigDecimal paymentAmt;
}
