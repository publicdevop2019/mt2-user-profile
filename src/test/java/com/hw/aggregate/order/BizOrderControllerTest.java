package com.hw.aggregate.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.command.CreateBizOrderCommand;
import com.hw.aggregate.order.command.PlaceBizOrderAgainCommand;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderItemAddOn;
import com.hw.aggregate.order.model.BizOrderItemAddOnSelection;
import com.hw.aggregate.order.representation.BizOrderPaymentLinkRepresentation;
import com.hw.aggregate.order.representation.BizOrderSummaryAdminRepresentation;
import com.hw.aggregate.order.representation.BizOrderSummaryCustomerRepresentation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.hw.aggregate.Helper.rJwt;
import static com.hw.aggregate.Helper.rLong;
import static org.mockito.ArgumentMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class BizOrderControllerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    BizOrderController orderController;

    @Mock
    BizOrderApplicationService orderApplicationService;

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
    @Ignore("see above")
    public void getAllOrdersForAdmin() throws JsonProcessingException {
        List<BizOrder> customerOrders = new ArrayList<>();
        for (int a = 0; a < 600; a++) {
            customerOrders.add(getCustomerOrder());
        }
        BizOrderSummaryAdminRepresentation customerOrders1 = new BizOrderSummaryAdminRepresentation(customerOrders);
        ReflectionTestUtils.setField(orderController, "objectMapper", objectMapper);
        long start = System.currentTimeMillis();
        Mockito.doReturn(customerOrders1).when(orderApplicationService).getAllOrdersForAdmin();
        ResponseEntity<?> allOrdersForAdmin = orderController.getAllOrdersForAdmin();
        Assert.assertEquals(HttpStatus.OK, allOrdersForAdmin.getStatusCode());
        Object body = allOrdersForAdmin.getBody();
        log.info("elapse:: " + (System.currentTimeMillis() - start));
        String s = objectMapper.writeValueAsString(body);
        Assert.assertNotNull(s);
    }

    private BizOrder getCustomerOrder() {
        BizOrder customerOrder = new BizOrder();
        List<BizOrderItem> customerOrderItems = new ArrayList<>();
        for (int a = 0; a < 5; a++) {
            customerOrderItems.add(getCustomerOrderItem());
        }
        customerOrder.setReadOnlyProductList(new ArrayList<>(customerOrderItems));
        customerOrder.setPaymentAmt(new BigDecimal(100));
        customerOrder.setId(1500L);
        return customerOrder;
    }

    private BizOrderItem getCustomerOrderItem() {
        BizOrderItem customerOrderItem = new BizOrderItem();

        customerOrderItem.setFinalPrice(getRandomString());
        customerOrderItem.setImageUrlSmall(getRandomString());
        customerOrderItem.setName(getRandomString());
        customerOrderItem.setProductId(getRandomString());

        List<BizOrderItemAddOn> customerOrderItemAddOns = new ArrayList<>();
        BizOrderItemAddOn customerOrderItemAddOn0 = new BizOrderItemAddOn();
        BizOrderItemAddOn customerOrderItemAddOn1 = new BizOrderItemAddOn();
        BizOrderItemAddOn customerOrderItemAddOn2 = new BizOrderItemAddOn();
        customerOrderItemAddOn0.setTitle(getRandomString());
        customerOrderItemAddOn1.setTitle(getRandomString());
        customerOrderItemAddOn2.setTitle(getRandomString());
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections0 = new ArrayList<>();
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections1 = new ArrayList<>();
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections2 = new ArrayList<>();
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection0 = new BizOrderItemAddOnSelection(getRandomString(), getRandomString());
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection1 = new BizOrderItemAddOnSelection(getRandomString(), getRandomString());
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection2 = new BizOrderItemAddOnSelection(getRandomString(), getRandomString());
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

    @Test
    public void testGetAllOrdersForAdmin() {
        BizOrderSummaryAdminRepresentation mock = Mockito.mock(BizOrderSummaryAdminRepresentation.class);
        Mockito.doReturn(mock).when(orderApplicationService).getAllOrdersForAdmin();
        ResponseEntity<List<BizOrderSummaryAdminRepresentation.BizOrderAdminCardRepresentation>> allOrdersForAdmin = orderController.getAllOrdersForAdmin();
        Mockito.verify(orderApplicationService, Mockito.times(1)).getAllOrdersForAdmin();
    }

    @Test
    public void getAllOrdersForCustomer() {
        BizOrderSummaryCustomerRepresentation mock = Mockito.mock(BizOrderSummaryCustomerRepresentation.class);
        Mockito.doReturn(mock).when(orderApplicationService).getAllOrders(anyString(), anyLong());
        ResponseEntity<List<BizOrderSummaryCustomerRepresentation.BizOrderCustomerBriefRepresentation>> allOrdersForCustomer = orderController.getAllOrdersForCustomer(rJwt(), rLong());
        Mockito.verify(orderApplicationService, Mockito.times(1)).getAllOrders(anyString(), anyLong());
    }

    @Test
    public void reserveOrder() {
        BizOrderPaymentLinkRepresentation mock = Mockito.mock(BizOrderPaymentLinkRepresentation.class);
        Mockito.doReturn(mock).when(orderApplicationService).createNew(anyString(), anyLong(),anyLong(), any(CreateBizOrderCommand.class));
        Mockito.doReturn("mock").when(mock).getPaymentLink();
        ResponseEntity<Void> voidResponseEntity = orderController.reserveOrder(rJwt(), rLong(),rLong(), new CreateBizOrderCommand());
        Assert.assertNotNull(voidResponseEntity.getHeaders().getLocation());
    }

    @Test
    public void getOrderById() {
        com.hw.aggregate.order.representation.BizOrderCustomerRepresentation mock = Mockito.mock(com.hw.aggregate.order.representation.BizOrderCustomerRepresentation.class);
        Mockito.doReturn(mock).when(orderApplicationService).getOrderForCustomer(anyString(), anyLong(), anyLong());

        ResponseEntity<com.hw.aggregate.order.representation.BizOrderCustomerRepresentation> orderById = orderController.getOrderById(rJwt(), rLong(), rLong());
        Assert.assertNotNull(orderById.getBody());
    }

    @Test
    public void placeOrderAgain() {
        BizOrderPaymentLinkRepresentation mock = Mockito.mock(BizOrderPaymentLinkRepresentation.class);
        Mockito.doReturn(mock).when(orderApplicationService).reserveAgain(anyString(), anyLong(), anyLong(), any(PlaceBizOrderAgainCommand.class));
        Mockito.doReturn("").when(mock).getPaymentLink();
        ResponseEntity<Void> voidResponseEntity = orderController.reserveAgain(rJwt(), rLong(), rLong(), new PlaceBizOrderAgainCommand());
        Assert.assertNotNull(voidResponseEntity.getHeaders().getLocation());
    }

    @Test
    public void deleteOrder() {
        Mockito.doNothing().when(orderApplicationService).deleteOrder(anyString(), anyLong(), anyLong());
        ResponseEntity<Void> voidResponseEntity = orderController.deleteOrder(rJwt(), rLong(), rLong());
        Mockito.verify(orderApplicationService, Mockito.times(1)).deleteOrder(anyString(), anyLong(), anyLong());
    }
}