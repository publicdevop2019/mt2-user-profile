package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.AddCartItemCommand;
import com.hw.aggregate.cart.command.DeleteCartItemCommand;
import com.hw.aggregate.cart.command.UpdateCartItemAddOnCommand;
import com.hw.aggregate.cart.exception.CartItemNotExistException;
import com.hw.aggregate.cart.exception.MaxCartItemException;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.cart.representation.CartItemRepresentation;
import com.hw.aggregate.cart.representation.CartSummaryRepresentation;
import com.hw.aggregate.profile.ProfileRepo;
import com.hw.aggregate.profile.model.Profile;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartApplicationService {

    @Autowired
    private ProfileRepo profileRepo;

    @Transactional(readOnly = true)
    public CartSummaryRepresentation getCartItems(Long profileId) {
        Optional<Profile> profileByResourceOwnerId = profileRepo.findById(profileId);
        return new CartSummaryRepresentation(profileByResourceOwnerId.get().getCartList());
    }

    @Transactional
    public CartItemRepresentation addCartItem(Long profileId, AddCartItemCommand addCartItemCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        if (findById.get().getCartList() == null)
            findById.get().setCartList(new ArrayList<>());
        if (findById.get().getCartList().size() == 5)
            throw new MaxCartItemException();
        findById.get().getCartList().add(addCartItemCommand);
        Profile save = profileRepo.save(findById.get());
        return new CartItemRepresentation(save.getCartList().stream().filter(e -> e.equals(addCartItemCommand)).findFirst().get());
    }

    @Transactional
    public void updateCartItem(Long profileId, Long cartItemId, UpdateCartItemAddOnCommand updateCartItemAddOnCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CartItem> collect = findById.get().getCartList().stream().filter(e -> e.getId().equals(cartItemId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new CartItemNotExistException();
        CartItem oldCartItem = collect.get(0);
        BeanUtils.copyProperties(updateCartItemAddOnCommand, oldCartItem);
        profileRepo.save(findById.get());
    }

    @Transactional
    public void deleteCartItem(Long profileId, DeleteCartItemCommand deleteCartItemCommand) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CartItem> collect = findById.get().getCartList().stream().filter(e -> e.getId().equals(deleteCartItemCommand.cartItemId)).collect(Collectors.toList());
        if (collect.size() != 1)
            throw new CartItemNotExistException();
        CartItem tobeRemoved = collect.get(0);
        findById.get().getCartList().removeIf(e -> e.getId().equals(tobeRemoved.getId()));
        profileRepo.save(findById.get());
    }

    @Transactional
    public void clearCartItem(Long profileId) {
        Optional<Profile> findById = profileRepo.findById(profileId);
        List<CartItem> collect = findById.get().getCartList();
        if (collect != null)
            findById.get().getCartList().clear();
        profileRepo.save(findById.get());
    }
}
