//package com.hw.aggregate.order;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.hw.aggregate.order.command.CreateOrderCommand;
//import com.hw.aggregate.order.command.PlaceOrderAgainCommand;
//import com.hw.aggregate.order.model.CustomerOrder;
//import com.hw.aggregate.order.model.CustomerOrderItem;
//import com.hw.aggregate.order.model.CustomerOrderItemAddOn;
//import com.hw.aggregate.order.model.CustomerOrderItemAddOnSelection;
//import com.hw.aggregate.order.representation.*;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static com.hw.aggregate.Helper.rJwt;
//import static com.hw.aggregate.Helper.rLong;
//import static org.mockito.ArgumentMatchers.*;
//
//@RunWith(MockitoJUnitRunner.class)
//@Slf4j
//public class OrderControllerTest {
//
//    ObjectMapper objectMapper = new ObjectMapper();
//
//    @InjectMocks
//    OrderController orderController;
//
//    @Mock
//    OrderApplicationService orderApplicationService;
//
//    /**
//     * getOrders are running slow, below test prove
//     * Jackson performance is not an issue,
//     * too many SQL query actually cause the problem
//     * product_snapshot is in another table, when retrieve all,
//     * it create n*m query, which significantly slow
//     *
//     * @throws JsonProcessingException
//     */
//    @Test
//    @Ignore("see above")
//    public void getAllOrdersForAdmin() throws JsonProcessingException {
//        List<CustomerOrder> customerOrders = new ArrayList<>();
//        for (int a = 0; a < 600; a++) {
//            customerOrders.add(getCustomerOrder());
//        }
//        OrderSummaryAdminRepresentation customerOrders1 = new OrderSummaryAdminRepresentation(customerOrders);
//        ReflectionTestUtils.setField(orderController, "objectMapper", objectMapper);
//        long start = System.currentTimeMillis();
//        Mockito.doReturn(customerOrders1).when(orderApplicationService).getAllOrdersForAdmin();
//        ResponseEntity<?> allOrdersForAdmin = orderController.getAllOrdersForAdmin();
//        Assert.assertEquals(HttpStatus.OK, allOrdersForAdmin.getStatusCode());
//        Object body = allOrdersForAdmin.getBody();
//        log.info("elapse:: " + (System.currentTimeMillis() - start));
//        String s = objectMapper.writeValueAsString(body);
//        Assert.assertNotNull(s);
//    }
//
//    private CustomerOrder getCustomerOrder() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        List<CustomerOrderItem> customerOrderItems = new ArrayList<>();
//        for (int a = 0; a < 5; a++) {
//            customerOrderItems.add(getCustomerOrderItem());
//        }
//        customerOrder.setReadOnlyProductList(new ArrayList<>(customerOrderItems));
//        customerOrder.setPaymentAmt(new BigDecimal(100));
//        customerOrder.setId(1500L);
//        return customerOrder;
//    }
//
//    private CustomerOrderItem getCustomerOrderItem() {
//        CustomerOrderItem customerOrderItem = new CustomerOrderItem();
//
//        customerOrderItem.setFinalPrice(getRandomString());
//        customerOrderItem.setImageUrlSmall(getRandomString());
//        customerOrderItem.setName(getRandomString());
//        customerOrderItem.setProductId(getRandomString());
//
//        List<CustomerOrderItemAddOn> customerOrderItemAddOns = new ArrayList<>();
//        CustomerOrderItemAddOn customerOrderItemAddOn0 = new CustomerOrderItemAddOn();
//        CustomerOrderItemAddOn customerOrderItemAddOn1 = new CustomerOrderItemAddOn();
//        CustomerOrderItemAddOn customerOrderItemAddOn2 = new CustomerOrderItemAddOn();
//        customerOrderItemAddOn0.setTitle(getRandomString());
//        customerOrderItemAddOn1.setTitle(getRandomString());
//        customerOrderItemAddOn2.setTitle(getRandomString());
//        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections0 = new ArrayList<>();
//        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections1 = new ArrayList<>();
//        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections2 = new ArrayList<>();
//        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection0 = new CustomerOrderItemAddOnSelection(getRandomString(), getRandomString());
//        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection1 = new CustomerOrderItemAddOnSelection(getRandomString(), getRandomString());
//        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection2 = new CustomerOrderItemAddOnSelection(getRandomString(), getRandomString());
//        customerOrderItemAddOnSelections0.add(customerOrderItemAddOnSelection0);
//        customerOrderItemAddOnSelections1.add(customerOrderItemAddOnSelection1);
//        customerOrderItemAddOnSelections2.add(customerOrderItemAddOnSelection2);
//        customerOrderItemAddOn0.setOptions(customerOrderItemAddOnSelections0);
//        customerOrderItemAddOn1.setOptions(customerOrderItemAddOnSelections1);
//        customerOrderItemAddOn2.setOptions(customerOrderItemAddOnSelections2);
//        customerOrderItemAddOns.add(customerOrderItemAddOn0);
//        customerOrderItemAddOns.add(customerOrderItemAddOn1);
//        customerOrderItemAddOns.add(customerOrderItemAddOn2);
//        customerOrderItem.setSelectedOptions(customerOrderItemAddOns);
//        return customerOrderItem;
//    }
//
//    private String getRandomString() {
//        return UUID.randomUUID().toString();
//    }
//
//    @Test
//    public void testGetAllOrdersForAdmin() {
//        OrderSummaryAdminRepresentation mock = Mockito.mock(OrderSummaryAdminRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).getAllOrdersForAdmin();
//        ResponseEntity<List<OrderSummaryAdminRepresentation.OrderAdminRepresentation>> allOrdersForAdmin = orderController.getAllOrdersForAdmin();
//        Mockito.verify(orderApplicationService, Mockito.times(1)).getAllOrdersForAdmin();
//    }
//
//    @Test
//    public void getAllOrdersForCustomer() {
//        OrderSummaryCustomerRepresentation mock = Mockito.mock(OrderSummaryCustomerRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).getAllOrders(anyString(), anyLong());
//        ResponseEntity<List<OrderSummaryCustomerRepresentation.OrderCustomerRepresentation>> allOrdersForCustomer = orderController.getAllOrdersForCustomer(rJwt(), rLong());
//        Mockito.verify(orderApplicationService, Mockito.times(1)).getAllOrders(anyString(), anyLong());
//    }
//
//    @Test
//    public void reserveOrder() {
//        OrderPaymentLinkRepresentation mock = Mockito.mock(OrderPaymentLinkRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).createNew(anyString(), anyLong(), any(CreateOrderCommand.class));
//        Mockito.doReturn("mock").when(mock).getPaymentLink();
//        ResponseEntity<Void> voidResponseEntity = orderController.reserveOrder(rJwt(), rLong(), new CreateOrderCommand());
//        Assert.assertNotNull(voidResponseEntity.getHeaders().getLocation());
//    }
//
//    @Test
//    public void getOrderById() {
//        OrderCustomerRepresentation mock = Mockito.mock(OrderCustomerRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).getOrderForCustomer(anyString(), anyLong(), anyLong());
//
//        ResponseEntity<OrderCustomerRepresentation> orderById = orderController.getOrderById(rJwt(), rLong(), rLong());
//        Assert.assertNotNull(orderById.getBody());
//    }
//
//    @Test
//    public void confirmOrderPaymentStatus() {
//        OrderConfirmStatusRepresentation mock = Mockito.mock(OrderConfirmStatusRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).confirmPayment(anyString(), anyLong(), anyLong());
//        Mockito.doNothing().when(orderApplicationService).confirmOrder(anyString(), anyLong(), anyLong());
//        Mockito.doReturn(Boolean.TRUE).when(mock).getPaymentStatus();
//        ResponseEntity<OrderConfirmStatusRepresentation> orderConfirmStatusRepresentationResponseEntity = orderController.confirmOrderPaymentStatus(rJwt(), rLong(), rLong());
//        Mockito.verify(orderApplicationService, Mockito.times(1)).confirmOrder(anyString(), anyLong(), anyLong());
//    }
//
//    @Test
//    public void confirmOrderPaymentStatus_2() {
//        OrderConfirmStatusRepresentation mock = Mockito.mock(OrderConfirmStatusRepresentation.class);
//        OrderPaymentLinkRepresentation mock1 = Mockito.mock(OrderPaymentLinkRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).confirmPayment(anyString(), anyLong(), anyLong());
//        Mockito.doReturn(mock1).when(orderApplicationService).placeAgain(anyString(), anyLong(), anyLong(), any());
//        Mockito.doReturn(Boolean.FALSE).when(mock).getPaymentStatus();
//        ResponseEntity<OrderConfirmStatusRepresentation> orderConfirmStatusRepresentationResponseEntity = orderController.confirmOrderPaymentStatus(rJwt(), rLong(), rLong());
//        Mockito.verify(orderApplicationService, Mockito.times(1)).placeAgain(anyString(), anyLong(), anyLong(), any());
//    }
//
//    @Test
//    public void placeOrderAgain() {
//        OrderPaymentLinkRepresentation mock = Mockito.mock(OrderPaymentLinkRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).placeAgain(anyString(), anyLong(), anyLong(), any(PlaceOrderAgainCommand.class));
//        Mockito.doReturn(Boolean.FALSE).when(mock).getPaymentState();
//        Mockito.doReturn("").when(mock).getPaymentLink();
//        ResponseEntity<Void> voidResponseEntity = orderController.placeOrderAgain(rJwt(), rLong(), rLong(), new PlaceOrderAgainCommand());
//        Assert.assertNotNull(voidResponseEntity.getHeaders().getLocation());
//    }
//
//    @Test
//    public void placeOrderAgain_2() {
//        OrderPaymentLinkRepresentation mock = Mockito.mock(OrderPaymentLinkRepresentation.class);
//        Mockito.doReturn(mock).when(orderApplicationService).placeAgain(anyString(), anyLong(), anyLong(), any(PlaceOrderAgainCommand.class));
//        Mockito.doReturn(Boolean.TRUE).when(mock).getPaymentState();
//        Mockito.doReturn("").when(mock).getPaymentLink();
//        Mockito.doNothing().when(orderApplicationService).confirmOrder(anyString(), anyLong(), anyLong());
//        ResponseEntity<Void> voidResponseEntity = orderController.placeOrderAgain(rJwt(), rLong(), rLong(), new PlaceOrderAgainCommand());
//        Assert.assertNotNull(voidResponseEntity.getHeaders().getLocation());
//        Mockito.verify(orderApplicationService, Mockito.times(1)).confirmOrder(anyString(), anyLong(), anyLong());
//    }
//
//    @Test
//    public void deleteOrder() {
//        Mockito.doNothing().when(orderApplicationService).deleteOrder(anyString(), anyLong(), anyLong());
//        ResponseEntity<Void> voidResponseEntity = orderController.deleteOrder(rJwt(), rLong(), rLong());
//        Mockito.verify(orderApplicationService, Mockito.times(1)).deleteOrder(anyString(), anyLong(), anyLong());
//    }
//}