package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.AddCartItemCommand;
import com.hw.aggregate.cart.command.DeleteCartItemCommand;
import com.hw.aggregate.cart.command.UpdateCartItemAddOnCommand;
import com.hw.aggregate.cart.representation.CartItemRepresentation;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = "application/json")
public class CartController {
    @Autowired
    private CartApplicationService cartApplicationService;

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/cart")
    public ResponseEntity<?> getCartItems(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(cartApplicationService.getCartItems(profileId).cartItems);
    }

    @ProfileExistAndOwnerOnly
    @PostMapping("profiles/{profileId}/cart")
    public ResponseEntity<?> addCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody AddCartItemCommand newCartItem) {
        CartItemRepresentation cartItemRepresentation = cartApplicationService.addCartItem(profileId, newCartItem);
        return ResponseEntity.ok().header("Location", cartItemRepresentation.cartItemId).build();
    }

    @ProfileExistAndOwnerOnly
    @PutMapping("profiles/{profileId}/cart/{cartItemId}")
    public ResponseEntity<?> updateCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "cartItemId") Long cartItemId, @RequestBody UpdateCartItemAddOnCommand updatedCartItem) {
        cartApplicationService.updateCartItem(profileId, cartItemId, updatedCartItem);
        return ResponseEntity.ok().build();
    }

    @ProfileExistAndOwnerOnly
    @DeleteMapping("profiles/{profileId}/cart/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "cartItemId") Long cartItemId) {
        cartApplicationService.deleteCartItem(profileId, new DeleteCartItemCommand(cartItemId));
        return ResponseEntity.ok().build();
    }
}
