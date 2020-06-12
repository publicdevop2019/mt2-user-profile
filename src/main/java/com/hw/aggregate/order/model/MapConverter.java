package com.hw.aggregate.order.model;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapConverter implements AttributeConverter<Map<BizOrderStatus, String>, String> {
    @Override
    public String convertToDatabaseColumn(Map<BizOrderStatus, String> stringStringMap) {
        if (stringStringMap == null)
            return "";
        return stringStringMap.keySet().stream().map(e -> e.name() + ":" + stringStringMap.get(e)).collect(Collectors.joining(","));
    }

    @Override
    public Map<BizOrderStatus, String> convertToEntityAttribute(String s) {
        if (s.equals("")) {
            return null;
        }
        HashMap<BizOrderStatus, String> stringStringHashMap = new HashMap<>();
        Arrays.stream(s.split(",")).forEach(e -> {
            String[] split = e.split(":");
            stringStringHashMap.put(BizOrderStatus.valueOf(split[0]), split[1]);
        });
        return stringStringHashMap;
    }
}
