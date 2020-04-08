package com.hw.aggregate.order;

import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.representation.OrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.OrderPaymentLinkRepresentation;
import com.hw.aggregate.order.representation.OrderRepresentation;
import com.hw.clazz.ProfileExistAndOwnerOnly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class OrderController {

    @Autowired
    private OrderApplicationService orderService;

    @GetMapping("orders")
    public ResponseEntity<?> getAllOrdersForAdmin(@RequestHeader("authorization") String authorization) {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin().customerOrders);
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> getAllOrders(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(orderService.getAllOrders(profileId).orderList);
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> getOrderById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        OrderRepresentation orderById = orderService.getOrderById(profileId, orderId);
        return ResponseEntity.ok(orderById.customerOrder);
    }

    @ProfileExistAndOwnerOnly
    @PostMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> reserveOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody ReserveOrderCommand newOrder) {
        OrderPaymentLinkRepresentation orderPaymentLinkRepresentation = orderService.reserveOrder(profileId, newOrder);
        return ResponseEntity.ok().header("Location", orderPaymentLinkRepresentation.paymentLink).build();
    }

    @ProfileExistAndOwnerOnly
    @GetMapping("profiles/{profileId}/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        OrderConfirmStatusRepresentation orderConfirmStatusRepresentation = orderService.confirmOrderPaymentStatus(profileId, new ConfirmOrderPaymentCommand(orderId));
        return ResponseEntity.ok(orderConfirmStatusRepresentation);
    }

    @ProfileExistAndOwnerOnly
    @PutMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> updateOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId, @RequestBody UpdateOrderAdminCommand newOrder) {
        orderService.updateOrderAdmin(profileId, orderId, newOrder);
        return ResponseEntity.ok().build();
    }

    @ProfileExistAndOwnerOnly
    @PutMapping("profiles/{profileId}/orders/{orderId}/replace")
    public ResponseEntity<?> replaceOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId, @RequestBody PlaceOrderAgainCommand newOrder) {
        OrderPaymentLinkRepresentation orderPaymentLinkRepresentation = orderService.placeOrderAgain(profileId, orderId, newOrder);
        return ResponseEntity.ok().header("Location", orderPaymentLinkRepresentation.paymentLink).build();
    }

    @ProfileExistAndOwnerOnly
    @DeleteMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        orderService.deleteOrder(profileId, new DeleteOrderCustomerCommand(orderId));
        return ResponseEntity.ok().build();
    }

}
