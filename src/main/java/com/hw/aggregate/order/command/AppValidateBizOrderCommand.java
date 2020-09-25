package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.BizOrderItem;
import lombok.Data;

import java.util.List;

@Data
public class AppValidateBizOrderCommand {
    private List<BizOrderItem> productList;
}
