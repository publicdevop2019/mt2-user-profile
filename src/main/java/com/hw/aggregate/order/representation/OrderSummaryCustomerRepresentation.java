package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderItem;
import com.hw.aggregate.order.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderSummaryCustomerRepresentation {

    private List<OrderCustomerRepresentation> orderList;

    public OrderSummaryCustomerRepresentation(List<CustomerOrder> orderList) {
        this.orderList = orderList.stream().map(OrderSummaryCustomerRepresentation.OrderCustomerRepresentation::new).collect(Collectors.toList());
    }

    @Data
    public class OrderCustomerRepresentation {
        private Long id;
        private BigDecimal paymentAmt;
        private OrderStatus orderState;
        private List<CustomerOrderItem> productList;

        public OrderCustomerRepresentation(CustomerOrder customerOrder) {
            this.id = customerOrder.getId();
            this.paymentAmt = customerOrder.getPaymentAmt();
            this.orderState = customerOrder.getOrderState();
            this.productList = customerOrder.getReadOnlyProductList();
        }
    }
}
