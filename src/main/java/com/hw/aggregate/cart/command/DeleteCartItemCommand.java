package com.hw.aggregate.cart.command;

public class DeleteCartItemCommand {
    public Long cartItemId;

    public DeleteCartItemCommand(Long cartItemId) {
        this.cartItemId = cartItemId;
    }
}
