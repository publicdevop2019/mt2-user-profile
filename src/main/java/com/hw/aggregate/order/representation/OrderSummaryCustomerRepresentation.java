package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderSummaryCustomerRepresentation {

    private List<OrderCustomerRepresentation> orderList;

    public OrderSummaryCustomerRepresentation(List<BizOrder> orderList) {
        this.orderList = orderList.stream().map(OrderSummaryCustomerRepresentation.OrderCustomerRepresentation::new).collect(Collectors.toList());
    }

    @Data
    public class OrderCustomerRepresentation {
        private Long id;
        private BigDecimal paymentAmt;
        private BizOrderStatus orderState;
        private List<BizOrderItem> productList;

        public OrderCustomerRepresentation(BizOrder customerOrder) {
            this.id = customerOrder.getId();
            this.paymentAmt = customerOrder.getPaymentAmt();
            this.orderState = customerOrder.getOrderState();
            this.productList = customerOrder.getReadOnlyProductList();
        }
    }
}
