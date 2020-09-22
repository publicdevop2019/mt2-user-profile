package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.BizOrderAddressCmdRep;
import com.hw.aggregate.order.model.BizOrderItemCommand;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserCreateBizOrderCommand {
    private BizOrderAddressCmdRep address;
    private List<BizOrderItemCommand> productList;
    private String paymentType;
    private BigDecimal paymentAmt;
}
