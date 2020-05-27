package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.CreateCartItemCommand;
import com.hw.aggregate.cart.command.DeleteCartItemCommand;
import com.hw.aggregate.cart.command.UpdateCartItemAddOnCommand;
import com.hw.aggregate.cart.exception.CartItemAccessException;
import com.hw.aggregate.cart.exception.CartItemNotExistException;
import com.hw.aggregate.cart.exception.MaxCartItemException;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.cart.representation.CartItemRepresentation;
import com.hw.aggregate.cart.representation.CartSummaryRepresentation;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import com.hw.shared.IdGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartApplicationService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private IdGenerator idGenerator;

    @ProfileExistAndOwnerOnly
    @Transactional(readOnly = true)
    public CartSummaryRepresentation getCartItems(String authUserId, Long profileId) {
        return new CartSummaryRepresentation(cartRepository.findByProfileId(profileId));
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public CartItemRepresentation addCartItem(String authUserId, Long profileId, CreateCartItemCommand addCartItemCommand) {
        List<CartItem> byProfileId = cartRepository.findByProfileId(profileId);
        if (byProfileId.size() == 10)
            throw new MaxCartItemException();
        CartItem cartItem = CartItem.create(
                idGenerator.getId(),
                profileId, addCartItemCommand.getName(), addCartItemCommand.getSelectedOptions(),
                addCartItemCommand.getFinalPrice(), addCartItemCommand.getImageUrlSmall(), addCartItemCommand.getProductId());
        CartItem save = cartRepository.save(cartItem);
        return new CartItemRepresentation(save);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    //not used
    public void updateCartItem(String authUserId, Long profileId, Long cartItemId, UpdateCartItemAddOnCommand updateCartItemAddOnCommand) {
        CartItem cartItemForCustomer = getCartItemForCustomer(profileId, cartItemId);
        BeanUtils.copyProperties(updateCartItemAddOnCommand, cartItemForCustomer);
        cartRepository.save(cartItemForCustomer);
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteCartItem(String authUserId, Long profileId, DeleteCartItemCommand deleteCartItemCommand) {
        CartItem cartItemForCustomer = getCartItemForCustomer(profileId, deleteCartItemCommand.cartItemId);
        cartRepository.delete(cartItemForCustomer);
    }

    @Transactional
    public void clearCartItem(Long profileId) {
        List<CartItem> byProfileId = cartRepository.findByProfileId(profileId);
        cartRepository.deleteInBatch(byProfileId);
    }

    private CartItem getCartItemForCustomer(Long profileId, Long cartItemId) {
        Optional<CartItem> byId = cartRepository.findById(cartItemId);
        if (byId.isEmpty())
            throw new CartItemNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new CartItemAccessException();
        return byId.get();
    }
}
