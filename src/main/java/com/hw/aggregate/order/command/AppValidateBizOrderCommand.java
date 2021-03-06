package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.BizOrderItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AppValidateBizOrderCommand implements Serializable {
    private static final long serialVersionUID = 1;
    private List<BizOrderItem> productList;
}
