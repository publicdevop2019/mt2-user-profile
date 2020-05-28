package com.hw.aggregate.cart.command;

import lombok.Data;

@Data
public class DeleteCartItemCommand {
    private Long cartItemId;

    public DeleteCartItemCommand(Long cartItemId) {
        this.cartItemId = cartItemId;
    }
}
