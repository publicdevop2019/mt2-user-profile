package com.hw.aggregate.cart.representation;

import com.hw.aggregate.cart.model.BizCartItem;
import com.hw.aggregate.order.model.BizOrderItemAddOn;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Data
public class UserBizCartItemCardRep {
    private Long id;

    private String name;

    private List<BizOrderItemAddOn> selectedOptions;

    private String finalPrice;

    private String imageUrlSmall;

    private String productId;

    private Set<String> attributesSales;
    private HashMap<String, String> attrIdMap;

    public UserBizCartItemCardRep(BizCartItem bizCart) {
        this.id = bizCart.getId();
        this.name = bizCart.getName();
        this.selectedOptions = bizCart.getSelectedOptions();
        this.finalPrice = bizCart.getFinalPrice();
        this.imageUrlSmall = bizCart.getImageUrlSmall();
        this.productId = bizCart.getProductId();
        this.attributesSales = bizCart.getAttributesSales();
        this.attrIdMap = bizCart.getAttrIdMap();
    }

}
