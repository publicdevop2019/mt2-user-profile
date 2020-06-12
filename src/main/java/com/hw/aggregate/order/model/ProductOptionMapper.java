package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProductOptionMapper implements AttributeConverter<List<BizOrderItemAddOn>, String> {
    @Override
    public String convertToDatabaseColumn(List<BizOrderItemAddOn> productOptions) {
        /**
         *  e.g.
         *  qty:1&1=2&2+3&3,color:white&0.35=black&0.37
         */
        if (productOptions == null || productOptions.isEmpty())
            return "";
        return productOptions.stream().map(e -> e.getTitle() + ":" + e.getOptions().stream().map(el -> el.getOptionValue() + "&" + (el.getPriceVar() != null ? el.getPriceVar() : "")).collect(Collectors.joining("="))).collect(Collectors.joining(","));
    }

    @Override
    public List<BizOrderItemAddOn> convertToEntityAttribute(String s) {
        if (s.equals(""))
            return Collections.emptyList();
        List<BizOrderItemAddOn> optionList = new ArrayList<>();
        Arrays.stream(s.split(",")).forEach(e -> {
            BizOrderItemAddOn option1 = new BizOrderItemAddOn();
            option1.setTitle(e.split(":")[0]);
            String detail = e.split(":")[1];
            String[] split = detail.split("=");
            Arrays.stream(split).forEach(el -> {
                String[] split1 = el.split("&");
                BizOrderItemAddOnSelection option;
                if (split1.length == 1) {
                    option = new BizOrderItemAddOnSelection(split1[0], null);
                } else {
                    option = new BizOrderItemAddOnSelection(split1[0], split1[1]);
                }
                if (option1.getOptions() == null)
                    option1.setOptions(new ArrayList<>());
                option1.getOptions().add(option);
            });
            optionList.add(option1);
        });
        return optionList;
    }
}
