package com.hw.aggregate.order.model;

import lombok.Data;

import java.util.List;

@Data
public class CustomerOrderItemAddOnCommand {

    private String title;

    private List<CustomerOrderItemAddOnSelectionCommand> options;

}
