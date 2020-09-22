package com.hw.aggregate.cart.model;

import com.hw.aggregate.cart.UserBizCartApplicationService;
import com.hw.aggregate.cart.command.UserCreateBizCartItemCommand;
import com.hw.aggregate.cart.exception.MaxCartItemException;
import com.hw.aggregate.cart.representation.UserBizCartItemCardRep;
import com.hw.aggregate.order.model.BizOrderItemAddOn;
import com.hw.aggregate.order.model.ProductOptionMapper;
import com.hw.shared.Auditable;
import com.hw.shared.LinkedHashSetConverter;
import com.hw.shared.rest.IdBasedEntity;
import com.hw.shared.sql.SumPagedRep;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "biz_cart_item")
@Data
@NoArgsConstructor
public class BizCartItem extends Auditable implements IdBasedEntity {

    @Id
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 10000)
    @Convert(converter = ProductOptionMapper.class)
    private List<BizOrderItemAddOn> selectedOptions;

    @NotBlank
    @Column(nullable = false)
    private String finalPrice;

    private String imageUrlSmall;

    @NotBlank
    @Column(nullable = false)
    private String productId;

    @Convert(converter = LinkedHashSetConverter.class)
    private Set<String> attributesSales;

    private HashMap<String, String> attrIdMap;

    public static BizCartItem create(Long id, UserCreateBizCartItemCommand command, UserBizCartApplicationService userBizCartApplicationService) {
        SumPagedRep<UserBizCartItemCardRep> userBizCartItemCardRepSumPagedRep = userBizCartApplicationService.readByQuery(null, null, null);
        if (userBizCartItemCardRepSumPagedRep.getData().size() == 10)
            throw new MaxCartItemException();
        return new BizCartItem(id, command);
    }

    private BizCartItem(Long id, UserCreateBizCartItemCommand command) {
        this.id = id;
        this.name = command.getName();
        this.selectedOptions = command.getSelectedOptions();
        this.finalPrice = command.getFinalPrice();
        this.imageUrlSmall = command.getImageUrlSmall();
        this.productId = command.getProductId();
        this.attributesSales = command.getAttributesSales();
        if (command.getAttrIdMap() != null)
            this.attrIdMap = new HashMap<>(command.getAttrIdMap());
    }
}
