package com.hw.aggregate.order.command;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BizOrderItemAddOnCommand implements Serializable {
    private static final long serialVersionUID = 1;
    private String title;

    private List<BizOrderItemAddOnSelectionCommand> options;

}
