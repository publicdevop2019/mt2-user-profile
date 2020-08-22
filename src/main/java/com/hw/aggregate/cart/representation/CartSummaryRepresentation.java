package com.hw.aggregate.cart.representation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.order.model.BizOrderItemAddOn;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CartSummaryRepresentation {

    private List<CartSummaryCardRepresentation> cartItems;

    public CartSummaryRepresentation(List<CartItem> cartList) {
        cartItems = cartList.stream().map(CartSummaryCardRepresentation::new).collect(Collectors.toList());
    }

    @Data
    public static class CartSummaryCardRepresentation {
        private Long id;
        private String name;
        private List<BizOrderItemAddOn> selectedOptions;
        private String finalPrice;
        private String imageUrlSmall;
        private String productId;
        @JsonDeserialize(as= LinkedHashSet.class)//use linkedHashSet to keep order of elements as it is received
        private Set<String> attributesSales;
        private Map<String,String> attrIdMap;

        public CartSummaryCardRepresentation(CartItem cartItem) {
            this.id = cartItem.getId();
            this.name = cartItem.getName();
            this.selectedOptions = cartItem.getSelectedOptions();
            this.finalPrice = cartItem.getFinalPrice();
            this.imageUrlSmall = cartItem.getImageUrlSmall();
            this.productId = cartItem.getProductId();
            this.attributesSales = cartItem.getAttributesSales();
            this.attrIdMap = cartItem.getAttrIdMap();
        }
    }
}
