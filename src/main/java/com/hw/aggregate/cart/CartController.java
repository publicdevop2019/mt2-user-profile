package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.CreateCartItemCommand;
import com.hw.aggregate.cart.command.DeleteCartItemCommand;
import com.hw.aggregate.cart.command.UpdateCartItemAddOnCommand;
import com.hw.aggregate.cart.representation.CartItemRepresentation;
import com.hw.shared.ServiceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = "application/json")
public class CartController {
    @Autowired
    private CartApplicationService cartApplicationService;

    @GetMapping("profiles/{profileId}/cart")
    public ResponseEntity<?> getCartItems(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(cartApplicationService.getCartItems(ServiceUtility.getUserId(authorization), profileId).cartItems);
    }

    @PostMapping("profiles/{profileId}/cart")
    public ResponseEntity<?> addCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody CreateCartItemCommand newCartItem) {
        CartItemRepresentation cartItemRepresentation = cartApplicationService.addCartItem(ServiceUtility.getUserId(authorization), profileId, newCartItem);
        return ResponseEntity.ok().header("Location", cartItemRepresentation.cartItemId).build();
    }

    @PutMapping("profiles/{profileId}/cart/{cartItemId}")
    public ResponseEntity<?> updateCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "cartItemId") Long cartItemId, @RequestBody UpdateCartItemAddOnCommand updatedCartItem) {
        cartApplicationService.updateCartItem(ServiceUtility.getUserId(authorization), profileId, cartItemId, updatedCartItem);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("profiles/{profileId}/cart/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "cartItemId") Long cartItemId) {
        cartApplicationService.deleteCartItem(ServiceUtility.getUserId(authorization), profileId, new DeleteCartItemCommand(cartItemId));
        return ResponseEntity.ok().build();
    }
}
