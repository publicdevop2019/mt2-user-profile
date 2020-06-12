package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderSummaryAdminRepresentation {
    private List<OrderAdminRepresentation> adminRepresentations;

    public OrderSummaryAdminRepresentation(List<BizOrder> customerOrders) {
        adminRepresentations = customerOrders.stream().map(OrderAdminRepresentation::new).collect(Collectors.toList());
    }

    @Data
    public class OrderAdminRepresentation {
        private Long id;
        private BigDecimal paymentAmt;
        private BizOrderStatus orderState;
        private List<BizOrderItem> productList;

        public OrderAdminRepresentation(BizOrder customerOrder) {
            this.id = customerOrder.getId();
            this.paymentAmt = customerOrder.getPaymentAmt();
            this.orderState = customerOrder.getOrderState();
            this.productList = customerOrder.getReadOnlyProductList();
        }
    }
}
