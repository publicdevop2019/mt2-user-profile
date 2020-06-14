package com.hw.aggregate.order;

import com.hw.aggregate.order.command.CreateBizOrderCommand;
import com.hw.aggregate.order.command.PlaceBizOrderAgainCommand;
import com.hw.aggregate.order.representation.BizOrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.BizOrderSummaryAdminRepresentation;
import com.hw.aggregate.order.representation.BizOrderSummaryCustomerRepresentation;
import com.hw.shared.ServiceUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class BizOrderController {

    @Autowired
    private BizOrderApplicationService orderService;

    @GetMapping("orders")
    public ResponseEntity<List<BizOrderSummaryAdminRepresentation.BizOrderAdminCardRepresentation>> getAllOrdersForAdmin() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin().getAdminRepresentations());
    }

    @GetMapping("profiles/{profileId}/orders")
    public ResponseEntity<List<BizOrderSummaryCustomerRepresentation.BizOrderCustomerBriefRepresentation>> getAllOrdersForCustomer(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId) {
        return ResponseEntity.ok(orderService.getAllOrders(ServiceUtility.getUserId(authorization), profileId).getOrderList());
    }

    @PostMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<Void> reserveOrder(@RequestHeader("authorization") String authorization,
                                             @PathVariable(name = "profileId") Long profileId,
                                             @PathVariable(name = "orderId") Long orderId,
                                             @RequestBody CreateBizOrderCommand newOrder) {
        return ResponseEntity.ok().header("Location", orderService.createNew(ServiceUtility.getUserId(authorization), profileId, orderId, newOrder).getPaymentLink()).build();
    }

    @GetMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<com.hw.aggregate.order.representation.BizOrderCustomerRepresentation> getOrderById(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        return ResponseEntity.ok(orderService.getOrderForCustomer(ServiceUtility.getUserId(authorization), profileId, orderId));
    }

    @GetMapping("profiles/{profileId}/orders/id")
    public ResponseEntity<Void> getOrderId() {
        return ResponseEntity.ok().header("Location", orderService.getOrderId()).build();
    }

    @GetMapping("profiles/{profileId}/orders/{orderId}/confirm")
    public ResponseEntity<BizOrderConfirmStatusRepresentation> confirmOrderPaymentStatus(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        return ResponseEntity.ok(orderService.confirmPayment(ServiceUtility.getUserId(authorization), profileId, orderId));
    }

    @GetMapping("profiles/{profileId}/orders/scheduler/resubmit")
    public ResponseEntity<BizOrderConfirmStatusRepresentation> manualResubmit() {
        log.info("manually resubmit order");
        orderService.resubmitOrder();
        return ResponseEntity.ok().build();
    }

    @PutMapping("profiles/{profileId}/orders/{orderId}/replace")
    public ResponseEntity<Void> reserveAgain(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId, @RequestBody PlaceBizOrderAgainCommand newOrder) {
        return ResponseEntity.ok().header("Location", orderService.reserveAgain(ServiceUtility.getUserId(authorization), profileId, orderId, newOrder).getPaymentLink()).build();
    }

    @DeleteMapping("profiles/{profileId}/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(@RequestHeader("authorization") String authorization, @PathVariable(name = "profileId") Long profileId, @PathVariable(name = "orderId") Long orderId) {
        orderService.deleteOrder(ServiceUtility.getUserId(authorization), profileId, orderId);
        return ResponseEntity.ok().build();
    }
}
