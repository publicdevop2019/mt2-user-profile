package com.hw.aggregate.order.representation;

import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderItem;
import com.hw.aggregate.order.model.CustomerOrderPaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderSummaryAdminRepresentation {
    private List<OrderAdminRepresentation> adminRepresentations;

    public OrderSummaryAdminRepresentation(List<CustomerOrder> customerOrders) {
        adminRepresentations = customerOrders.stream().map(OrderAdminRepresentation::new).collect(Collectors.toList());
    }

    @Data
    public class OrderAdminRepresentation {
        private Long id;
        private BigDecimal paymentAmt;
        private CustomerOrderPaymentStatus paymentStatus;
        private List<CustomerOrderItem> productList;

        public OrderAdminRepresentation(CustomerOrder customerOrder) {
            this.id = customerOrder.getId();
            this.paymentAmt = customerOrder.getPaymentAmt();
            this.paymentStatus = customerOrder.getPaymentStatus();
            this.productList = customerOrder.getProductList();
        }
    }
}