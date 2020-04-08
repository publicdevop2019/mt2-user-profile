package com.hw.aggregate.order;

import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.representation.OrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.OrderPaymentLinkRepresentation;
import com.hw.aggregate.order.representation.OrderRepresentation;
import com.hw.shared.ServiceUtility;
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
    public ResponseEntity<?> getAllOrdersForAdmin() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin().customerOrders);
    }

    @GetMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> getAllOrders(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(orderService.getAllOrders(ServiceUtility.getUserId(authorization), profileId).orderList);
    }

    @PostMapping("profiles/{profileId}/orders")
    public ResponseEntity<?> reserveOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody ReserveOrderCommand newOrder) {
        OrderPaymentLinkRepresentation orderPaymentLinkRepresentation = orderService.reserveOrder(ServiceUtility.getUserId(authorization), profileId, newOrder);
        return ResponseEntity.ok().header("Location", orderPaymentLinkRepresentation.paymentLink).build();
    }

    @GetMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> getOrderById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        OrderRepresentation orderById = orderService.getOrderById(ServiceUtility.getUserId(authorization), profileId, orderId);
        return ResponseEntity.ok(orderById.customerOrder);
    }

    @GetMapping("profiles/{profileId}/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrderPaymentStatus(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        OrderConfirmStatusRepresentation orderConfirmStatusRepresentation = orderService.confirmOrderPaymentStatus(ServiceUtility.getUserId(authorization), profileId, new ConfirmOrderPaymentCommand(orderId));
        return ResponseEntity.ok(orderConfirmStatusRepresentation);
    }

    //not used
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<?> updateOrderAdmin(@PathVariable(name = "orderId") Long orderId, @RequestBody UpdateOrderAdminCommand newOrder) {
        orderService.updateOrderAdmin(orderId, newOrder);
        return ResponseEntity.ok().build();
    }

    @PutMapping("profiles/{profileId}/orders/{orderId}/replace")
    public ResponseEntity<?> placeOrderAgain(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId, @RequestBody PlaceOrderAgainCommand newOrder) {
        OrderPaymentLinkRepresentation orderPaymentLinkRepresentation = orderService.placeOrderAgain(ServiceUtility.getUserId(authorization), profileId, orderId, newOrder);
        return ResponseEntity.ok().header("Location", orderPaymentLinkRepresentation.paymentLink).build();
    }

    @DeleteMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        orderService.deleteOrder(ServiceUtility.getUserId(authorization), profileId, new DeleteOrderCustomerCommand(orderId));
        return ResponseEntity.ok().build();
    }

}
