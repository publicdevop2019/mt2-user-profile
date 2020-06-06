package com.hw.aggregate.order;

import com.hw.aggregate.order.command.CreateOrderCommand;
import com.hw.aggregate.order.command.PlaceOrderAgainCommand;
import com.hw.aggregate.order.representation.OrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.OrderCustomerRepresentation;
import com.hw.aggregate.order.representation.OrderSummaryAdminRepresentation;
import com.hw.aggregate.order.representation.OrderSummaryCustomerRepresentation;
import com.hw.shared.ServiceUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class OrderController {

    @Autowired
    private OrderApplicationService orderService;

    @GetMapping("orders")
    public ResponseEntity<List<OrderSummaryAdminRepresentation.OrderAdminRepresentation>> getAllOrdersForAdmin() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin().getAdminRepresentations());
    }

    @GetMapping("profiles/{profileId}/orders")
    public ResponseEntity<List<OrderSummaryCustomerRepresentation.OrderCustomerRepresentation>> getAllOrdersForCustomer(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(orderService.getAllOrders(ServiceUtility.getUserId(authorization), profileId).getOrderList());
    }

    @PostMapping("profiles/{profileId}/orders")
    public ResponseEntity<Void> reserveOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @RequestBody CreateOrderCommand newOrder) {
        return ResponseEntity.ok().header("Location", orderService.createNew(ServiceUtility.getUserId(authorization), profileId, newOrder).getPaymentLink()).build();
    }

    @GetMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<OrderCustomerRepresentation> getOrderById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        return ResponseEntity.ok(orderService.getOrderForCustomer(ServiceUtility.getUserId(authorization), profileId, orderId));
    }

    @GetMapping("profiles/{profileId}/orders/{orderId}/confirm")
    public ResponseEntity<OrderConfirmStatusRepresentation> confirmOrderPaymentStatus(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        return ResponseEntity.ok(orderService.confirmPayment(ServiceUtility.getUserId(authorization), profileId, orderId));
    }

    @PutMapping("profiles/{profileId}/orders/{orderId}/replace")
    public ResponseEntity<Void> placeOrderAgain(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId, @RequestBody PlaceOrderAgainCommand newOrder) {
        return ResponseEntity.ok().header("Location", orderService.reserveAgain(ServiceUtility.getUserId(authorization), profileId, orderId, newOrder).getPaymentLink()).build();
    }

    @DeleteMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        orderService.deleteOrder(ServiceUtility.getUserId(authorization), profileId, orderId);
        return ResponseEntity.ok().build();
    }
}
