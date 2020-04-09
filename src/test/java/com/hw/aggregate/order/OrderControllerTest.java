package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.model.CustomerOrder;
import com.hw.aggregate.order.model.CustomerOrderItem;
import com.hw.aggregate.order.model.CustomerOrderItemAddOn;
import com.hw.aggregate.order.model.CustomerOrderItemAddOnSelection;
import com.hw.aggregate.order.representation.OrderSummaryAdminRepresentation;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    OrderController orderController = new OrderController();

    @Mock
    OrderApplicationService orderApplicationService;

    /**
     * getOrders are running slow, below test prove
     * Jackson performance is not an issue,
     * too many SQL query actually cause the problem
     * product_snapshot is in another table, when retrieve all,
     * it create n*m query, which significantly slow
     *
     * @throws JsonProcessingException
     */
    @Test
    @Ignore
    public void getAllOrdersForAdmin() throws JsonProcessingException {
        List<CustomerOrder> customerOrders = new ArrayList<>();
        for (int a = 0; a < 600; a++) {
            customerOrders.add(getCustomerOrder());
        }
        OrderSummaryAdminRepresentation customerOrders1 = new OrderSummaryAdminRepresentation(customerOrders);
        ReflectionTestUtils.setField(orderController, "objectMapper", objectMapper);
        long start = System.currentTimeMillis();
        Mockito.doReturn(customerOrders1).when(orderApplicationService).getAllOrdersForAdmin();
        ResponseEntity<?> allOrdersForAdmin = orderController.getAllOrdersForAdmin();
        Object body = allOrdersForAdmin.getBody();
        System.out.println("elapse:: " + (System.currentTimeMillis() - start));
        String s = objectMapper.writeValueAsString(body);
        System.out.println("output:: " + s);


    }

    private CustomerOrder getCustomerOrder() {
        CustomerOrder customerOrder = new CustomerOrder();
        List<CustomerOrderItem> customerOrderItems = new ArrayList<>();
        for (int a = 0; a < 5; a++) {
            customerOrderItems.add(getCustomerOrderItem());
        }
        customerOrder.setReadOnlyProductList(new ArrayList<>(customerOrderItems));
        customerOrder.setPaymentAmt(new BigDecimal(100));
        customerOrder.setPaymentStatus(true);
        customerOrder.setId(1500L);
        return customerOrder;
    }

    private CustomerOrderItem getCustomerOrderItem() {
        CustomerOrderItem customerOrderItem = new CustomerOrderItem();

        customerOrderItem.setFinalPrice(getRandomString());
        customerOrderItem.setImageUrlSmall(getRandomString());
        customerOrderItem.setName(getRandomString());
        customerOrderItem.setProductId(getRandomString());

        List<CustomerOrderItemAddOn> customerOrderItemAddOns = new ArrayList<>();
        CustomerOrderItemAddOn customerOrderItemAddOn0 = new CustomerOrderItemAddOn();
        CustomerOrderItemAddOn customerOrderItemAddOn1 = new CustomerOrderItemAddOn();
        CustomerOrderItemAddOn customerOrderItemAddOn2 = new CustomerOrderItemAddOn();
        customerOrderItemAddOn0.setTitle(getRandomString());
        customerOrderItemAddOn1.setTitle(getRandomString());
        customerOrderItemAddOn2.setTitle(getRandomString());
        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections0 = new ArrayList<>();
        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections1 = new ArrayList<>();
        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections2 = new ArrayList<>();
        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection0 = new CustomerOrderItemAddOnSelection(getRandomString(), getRandomString());
        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection1 = new CustomerOrderItemAddOnSelection(getRandomString(), getRandomString());
        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection2 = new CustomerOrderItemAddOnSelection(getRandomString(), getRandomString());
        customerOrderItemAddOnSelections0.add(customerOrderItemAddOnSelection0);
        customerOrderItemAddOnSelections1.add(customerOrderItemAddOnSelection1);
        customerOrderItemAddOnSelections2.add(customerOrderItemAddOnSelection2);
        customerOrderItemAddOn0.setOptions(customerOrderItemAddOnSelections0);
        customerOrderItemAddOn1.setOptions(customerOrderItemAddOnSelections1);
        customerOrderItemAddOn2.setOptions(customerOrderItemAddOnSelections2);
        customerOrderItemAddOns.add(customerOrderItemAddOn0);
        customerOrderItemAddOns.add(customerOrderItemAddOn1);
        customerOrderItemAddOns.add(customerOrderItemAddOn2);
        customerOrderItem.setSelectedOptions(customerOrderItemAddOns);
        return customerOrderItem;
    }

    private String getRandomString() {
        return UUID.randomUUID().toString();
    }
}