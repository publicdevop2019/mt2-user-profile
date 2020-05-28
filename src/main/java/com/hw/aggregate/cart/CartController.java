package com.hw.aggregate.cart;

import com.hw.aggregate.cart.command.CreateCartItemCommand;
import com.hw.aggregate.cart.command.DeleteCartItemCommand;
import com.hw.aggregate.cart.model.CartItem;
import com.hw.aggregate.cart.representation.CartItemRepresentation;
import com.hw.shared.ServiceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = "application/json")
public class CartController {
    @Autowired
    private CartApplicationService cartApplicationService;

    @GetMapping("profiles/{profileId}/cart")
    public ResponseEntity<List<CartItem>> getCartItems(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(cartApplicationService.getCartItems(ServiceUtility.getUserId(authorization), profileId).getCartItems());
    }

    @PostMapping("profiles/{profileId}/cart")
    public ResponseEntity<Void> addCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody CreateCartItemCommand newCartItem) {
        CartItemRepresentation cartItemRepresentation = cartApplicationService.addCartItem(ServiceUtility.getUserId(authorization), profileId, newCartItem);
        return ResponseEntity.ok().header("Location", cartItemRepresentation.getCartItemId()).build();
    }

    @DeleteMapping("profiles/{profileId}/cart/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "cartItemId") Long cartItemId) {
        cartApplicationService.deleteCartItem(ServiceUtility.getUserId(authorization), profileId, new DeleteCartItemCommand(cartItemId));
        return ResponseEntity.ok().build();
    }
}
