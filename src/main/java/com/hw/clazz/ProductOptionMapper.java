package com.hw.clazz;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProductOptionMapper implements AttributeConverter<List<ProductOption>, String> {
    @Override
    public String convertToDatabaseColumn(List<ProductOption> productOptions) {
        /**
         *  e.g.
         *  qty:1&1*2&2*3&3,color:white&0.35*black&0.37
         */

        return productOptions.stream().map(e -> e.title + ":" + e.options.stream().map(el -> el.optionValue + "&" + el.priceVar).collect(Collectors.joining("="))).collect(Collectors.joining(","));
    }

    @Override
    public List<ProductOption> convertToEntityAttribute(String s) {
        List<ProductOption> optionList = new ArrayList<>();
        Arrays.stream(s.split(",")).forEach(e -> {
            ProductOption option1 = new ProductOption();
            option1.title = e.split(":")[0];
            String detail = e.split(":")[1];
            String[] split = detail.split("=");
            Arrays.stream(split).forEach(el -> {
                String[] split1 = el.split("&");
                OptionItem option = new OptionItem(split1[0], split1[1]);
                if (option1.options == null)
                    option1.options = new ArrayList<>();
                option1.options.add(option);
            });
            optionList.add(option1);
        });
        return optionList;
    }
}
