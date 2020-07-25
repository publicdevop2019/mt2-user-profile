package com.hw.aggregate.cart.model;

import com.hw.aggregate.cart.CartRepository;
import com.hw.aggregate.cart.command.CreateCartItemCommand;
import com.hw.aggregate.cart.exception.CartItemAccessException;
import com.hw.aggregate.cart.exception.CartItemNotExistException;
import com.hw.aggregate.cart.exception.MaxCartItemException;
import com.hw.aggregate.order.model.BizOrderItemAddOn;
import com.hw.aggregate.order.model.ProductOptionMapper;
import com.hw.aggregate.order.model.StringSetConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "Cart")
@Data
@NoArgsConstructor
public class CartItem {

    @Id
    private Long id;

    @Column(name = "fk_profile")
    private Long profileId;

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

    @Convert(converter = StringSetConverter.class)
    private Set<String> attributesSales;

    private HashMap<String, String> attrIdMap;

    public static CartItem create(Long id, Long profileId, CreateCartItemCommand command, CartRepository cartRepository) {
        List<CartItem> byProfileId = cartRepository.findByProfileId(profileId);
        if (byProfileId.size() == 10)
            throw new MaxCartItemException();
        return cartRepository.save(new CartItem(id, profileId, command));
    }

    private CartItem(Long id, Long profileId, CreateCartItemCommand command) {
        this.id = id;
        this.profileId = profileId;
        this.name = command.getName();
        this.selectedOptions = command.getSelectedOptions();
        this.finalPrice = command.getFinalPrice();
        this.imageUrlSmall = command.getImageUrlSmall();
        this.productId = command.getProductId();
        this.attributesSales = command.getAttributesSales();
        if (command.getAttrIdMap() != null)
            this.attrIdMap = new HashMap<>(command.getAttrIdMap());
    }

    public static CartItem get(Long profileId, Long cartItemId, CartRepository cartRepository) {
        Optional<CartItem> byId = cartRepository.findById(cartItemId);
        if (byId.isEmpty())
            throw new CartItemNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new CartItemAccessException();
        return byId.get();
    }

    public static void delete(Long profileId, Long cartItemId, CartRepository cartRepository) {
        CartItem.get(profileId, cartItemId, cartRepository);
        cartRepository.deleteById(cartItemId);
    }

}
