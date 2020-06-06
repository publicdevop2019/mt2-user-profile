package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.CreateCartItemCommand;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.cart.representation.CartItemRepresentation;
import com.hw.aggregate.cart.representation.CartSummaryRepresentation;
import com.hw.config.ProfileExistAndOwnerOnly;
import com.hw.shared.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return new CartItemRepresentation(CartItem.create(idGenerator.getId(), profileId, addCartItemCommand, cartRepository));
    }

    @ProfileExistAndOwnerOnly
    @Transactional
    public void deleteCartItem(String authUserId, Long profileId, Long cartItemId) {
        CartItem.delete(profileId, cartItemId, cartRepository);
    }

    @Transactional
    public void clearCartItem(Long profileId) {
        cartRepository.deleteInBatch(cartRepository.findByProfileId(profileId));
    }

    @Transactional
    public void rollbackTransaction(String transactionId) {
    }
}
