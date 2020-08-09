package com.hw.aggregate.order.model.product;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class AppProductOption {
    public String title;
    public List<OptionItem> options;

    @Data
    @AllArgsConstructor
    public static class OptionItem {
        public String optionValue;
        public String priceVar;
    }

}
