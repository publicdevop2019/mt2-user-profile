package com.hw.aggregate.order.model;

import lombok.Data;

import java.util.List;

@Data
public class BizOrderItemAddOnCommand {

    private String title;

    private List<BizOrderItemAddOnSelectionCommand> options;

}
