package com.hw.aggregate.order;

import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.command.CreateOrderCommand;
import com.hw.aggregate.order.command.PlaceOrderAgainCommand;
import com.hw.aggregate.order.exception.StateChangeException;
import com.hw.aggregate.order.model.*;
import com.hw.aggregate.order.representation.OrderConfirmStatusRepresentation;
import com.hw.aggregate.order.representation.OrderPaymentLinkRepresentation;
import com.hw.shared.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.hw.aggregate.Helper.rLong;
import static com.hw.aggregate.Helper.rStr;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class OrderApplicationServiceTest {
    @InjectMocks
    OrderApplicationService orderApplicationService = new OrderApplicationService();
    @Mock
    IdGenerator idGenerator;
    @Mock
    Executor customExecutor;
    @Mock
    ProductService productService;
    @Mock
    PaymentService paymentService;
    @Mock
    CartApplicationService cartApplicationService;
    @Mock
    OrderRepository orderRepository;
    @Mock
    CompletableFuture<Void> completableFuture;
    @Mock
    MessengerService messengerService;
    @Mock
    PlatformTransactionManager platformTransactionManager;
    @Mock
    EntityManager entityManager;

    @Test
    public void createNew() {
        CustomerOrder customerOrder = getCustomerOrder();
        when(idGenerator.getId()).thenReturn(rLong());
        Mockito.doAnswer(
                (InvocationOnMock invocation) -> {
                    ((Runnable) invocation.getArguments()[0]).run();
                    return completableFuture;
                }
        ).when(customExecutor).execute(any(Runnable.class));
        Mockito.doNothing().when(productService).validateProductInfo(any(List.class));
        Mockito.doReturn("Test").when(paymentService).generatePaymentLink(anyString());
        Mockito.doNothing().when(productService).decreaseOrderStorage(anyMap(), anyString());
        Mockito.doNothing().when(cartApplicationService).clearCartItem(anyLong());
        Mockito.doReturn(customerOrder).when(orderRepository).saveAndFlush(any(CustomerOrder.class));

        CreateOrderCommand createOrderCommand = new CreateOrderCommand();
        CustomerOrderAddressCommand addressCommand = new CustomerOrderAddressCommand();
        createOrderCommand.setAddress(addressCommand);
        createOrderCommand.setPaymentAmt(customerOrder.getPaymentAmt());
        createOrderCommand.setPaymentType(customerOrder.getPaymentType());
        createOrderCommand.setProductList(List.of(getCustomerOrderItemCommand()));

        OrderPaymentLinkRepresentation aNew = orderApplicationService.createNew(rStr(), rLong(), createOrderCommand);
        Assert.assertNotNull(aNew);
        Assert.assertNotEquals("", aNew.getPaymentLink());
        Assert.assertNotEquals(Boolean.TRUE, aNew.getPaymentState());
    }

    @Test
    public void confirmPayment() {
        Long aLong = rLong();
        CustomerOrder customerOrder = getCustomerOrder();
        customerOrder.setCreatedBy(aLong.toString());
        customerOrder.setProfileId(aLong);
        customerOrder.setOrderState(OrderState.NOT_PAID_RECYCLED);
        Mockito.doReturn(Optional.of(customerOrder)).when(orderRepository).findByIdForUpdate(anyLong());
        Mockito.doReturn(Boolean.TRUE).when(paymentService).confirmPaymentStatus(anyString());
        OrderConfirmStatusRepresentation orderConfirmStatusRepresentation = orderApplicationService.confirmPayment(rStr(), aLong, rLong());
        Assert.assertTrue(orderConfirmStatusRepresentation.getPaymentStatus());
    }

    @Test
    public void confirmOrder() {
        Long aLong = rLong();
        CustomerOrder customerOrder = getCustomerOrder();
        customerOrder.setCreatedBy(aLong.toString());
        customerOrder.setProfileId(aLong);
        customerOrder.setOrderState(OrderState.PAID_RESERVED);
        Mockito.doReturn(Optional.of(customerOrder)).when(orderRepository).findByIdForUpdate(anyLong());
        Mockito.doNothing().when(messengerService).notifyBusinessOwner(anyMap());
        Mockito.doAnswer(
                (InvocationOnMock invocation) -> {
                    ((Runnable) invocation.getArguments()[0]).run();
                    return completableFuture;
                }
        ).when(customExecutor).execute(any(Runnable.class));
        Mockito.doNothing().when(productService).decreaseActualStorage(anyMap(), anyString());
        Mockito.doNothing().when(entityManager).persist(any());
        orderApplicationService.confirmOrder(rStr(), aLong, rLong());
        Mockito.verify(messengerService, Mockito.times(1)).notifyBusinessOwner(anyMap());
        Mockito.verify(productService, Mockito.times(0)).rollbackChange(anyString());
        Assert.assertEquals(OrderState.CONFIRMED, customerOrder.getOrderState());
    }

    @Test(expected = StateChangeException.class)
    public void placeAgain_PAID_RESERVED() {
        Long aLong = rLong();
        CustomerOrder customerOrder = getCustomerOrder();
        customerOrder.setCreatedBy(aLong.toString());
        customerOrder.setProfileId(aLong);
        customerOrder.setOrderState(OrderState.PAID_RESERVED);
        Mockito.doReturn(Optional.of(customerOrder)).when(orderRepository).findByIdForUpdate(anyLong());
        PlaceOrderAgainCommand placeOrderAgainCommand = new PlaceOrderAgainCommand();
        orderApplicationService.placeAgain(rStr(), aLong, rLong(), placeOrderAgainCommand);
    }

    @Test(expected = StateChangeException.class)
    public void placeAgain_CONFIRMED() {
        Long aLong = rLong();
        CustomerOrder customerOrder = getCustomerOrder();
        customerOrder.setCreatedBy(aLong.toString());
        customerOrder.setProfileId(aLong);
        customerOrder.setOrderState(OrderState.CONFIRMED);
        Mockito.doReturn(Optional.of(customerOrder)).when(orderRepository).findByIdForUpdate(anyLong());
        PlaceOrderAgainCommand placeOrderAgainCommand = new PlaceOrderAgainCommand();
        orderApplicationService.placeAgain(rStr(), aLong, rLong(), placeOrderAgainCommand);
    }

    @Test
    public void placeAgain_PAID_RECYCLED() {
        Long aLong = rLong();
        CustomerOrder customerOrder = getCustomerOrder();
        customerOrder.setCreatedBy(aLong.toString());
        customerOrder.setProfileId(aLong);
        customerOrder.setOrderState(OrderState.PAID_RECYCLED);
        customerOrder.setPaymentLink(rStr());
        Mockito.doReturn(Optional.of(customerOrder)).when(orderRepository).findByIdForUpdate(anyLong());
        Mockito.doAnswer(
                (InvocationOnMock invocation) -> {
                    ((Runnable) invocation.getArguments()[0]).run();
                    return completableFuture;
                }
        ).when(customExecutor).execute(any(Runnable.class));
        Mockito.doNothing().when(productService).decreaseOrderStorage(anyMap(), anyString());
        Mockito.doReturn(customerOrder).when(orderRepository).saveAndFlush(any(CustomerOrder.class));
        PlaceOrderAgainCommand placeOrderAgainCommand = new PlaceOrderAgainCommand();
        OrderPaymentLinkRepresentation representation = orderApplicationService.placeAgain(rStr(), aLong, rLong(), placeOrderAgainCommand);
        Assert.assertEquals(OrderState.PAID_RESERVED, customerOrder.getOrderState());
        Assert.assertNotNull(representation.getPaymentLink());
        Assert.assertEquals(Boolean.TRUE, representation.getPaymentState());
    }

    @Test
    public void placeAgain_NOT_PAID_RESERVED() {
        Long aLong = rLong();
        CustomerOrder customerOrder = getCustomerOrder();
        customerOrder.setCreatedBy(aLong.toString());
        customerOrder.setProfileId(aLong);
        customerOrder.setOrderState(OrderState.NOT_PAID_RESERVED);
        customerOrder.setPaymentLink(rStr());
        Mockito.doReturn(Optional.of(customerOrder)).when(orderRepository).findByIdForUpdate(anyLong());
        Mockito.doReturn(customerOrder).when(orderRepository).saveAndFlush(any(CustomerOrder.class));
        PlaceOrderAgainCommand placeOrderAgainCommand = new PlaceOrderAgainCommand();
        OrderPaymentLinkRepresentation representation = orderApplicationService.placeAgain(rStr(), aLong, rLong(), placeOrderAgainCommand);
        Assert.assertEquals(OrderState.NOT_PAID_RESERVED, customerOrder.getOrderState());
        Assert.assertNotNull(representation.getPaymentLink());
        Assert.assertEquals(Boolean.FALSE, representation.getPaymentState());
    }

    private CustomerOrder getCustomerOrder() {
        CustomerOrder customerOrder = new CustomerOrder();
        List<CustomerOrderItem> customerOrderItems = new ArrayList<>();
        for (int a = 0; a < 5; a++) {
            customerOrderItems.add(getCustomerOrderItem());
        }
        customerOrder.setReadOnlyProductList(new ArrayList<>(customerOrderItems));
        customerOrder.setPaymentAmt(new BigDecimal(0));
        customerOrder.setId(1500L);

        return customerOrder;
    }

    private CustomerOrderItem getCustomerOrderItem() {
        CustomerOrderItem customerOrderItem = new CustomerOrderItem();

        customerOrderItem.setFinalPrice("0");
        customerOrderItem.setImageUrlSmall(rStr());
        customerOrderItem.setName(rStr());
        customerOrderItem.setProductId(rStr());

        List<CustomerOrderItemAddOn> customerOrderItemAddOns = new ArrayList<>();
        CustomerOrderItemAddOn customerOrderItemAddOn0 = new CustomerOrderItemAddOn();
        CustomerOrderItemAddOn customerOrderItemAddOn1 = new CustomerOrderItemAddOn();
        CustomerOrderItemAddOn customerOrderItemAddOn2 = new CustomerOrderItemAddOn();
        customerOrderItemAddOn0.setTitle(rStr());
        customerOrderItemAddOn1.setTitle(rStr());
        customerOrderItemAddOn2.setTitle(rStr());
        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections0 = new ArrayList<>();
        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections1 = new ArrayList<>();
        List<CustomerOrderItemAddOnSelection> customerOrderItemAddOnSelections2 = new ArrayList<>();
        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection0 = new CustomerOrderItemAddOnSelection(rStr(), "+0");
        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection1 = new CustomerOrderItemAddOnSelection(rStr(), "+0");
        CustomerOrderItemAddOnSelection customerOrderItemAddOnSelection2 = new CustomerOrderItemAddOnSelection(rStr(), "+0");
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

    private CustomerOrderItemCommand getCustomerOrderItemCommand() {
        CustomerOrderItemCommand customerOrderItem = new CustomerOrderItemCommand();

        customerOrderItem.setFinalPrice("0");
        customerOrderItem.setImageUrlSmall(rStr());
        customerOrderItem.setName(rStr());
        customerOrderItem.setProductId(rStr());

        List<CustomerOrderItemAddOnCommand> customerOrderItemAddOns = new ArrayList<>();
        CustomerOrderItemAddOnCommand customerOrderItemAddOn0 = new CustomerOrderItemAddOnCommand();
        CustomerOrderItemAddOnCommand customerOrderItemAddOn1 = new CustomerOrderItemAddOnCommand();
        CustomerOrderItemAddOnCommand customerOrderItemAddOn2 = new CustomerOrderItemAddOnCommand();
        customerOrderItemAddOn0.setTitle(rStr());
        customerOrderItemAddOn1.setTitle(rStr());
        customerOrderItemAddOn2.setTitle(rStr());
        List<CustomerOrderItemAddOnSelectionCommand> customerOrderItemAddOnSelections0 = new ArrayList<>();
        List<CustomerOrderItemAddOnSelectionCommand> customerOrderItemAddOnSelections1 = new ArrayList<>();
        List<CustomerOrderItemAddOnSelectionCommand> customerOrderItemAddOnSelections2 = new ArrayList<>();
        CustomerOrderItemAddOnSelectionCommand customerOrderItemAddOnSelection0 = new CustomerOrderItemAddOnSelectionCommand(rStr(), "+0");
        CustomerOrderItemAddOnSelectionCommand customerOrderItemAddOnSelection1 = new CustomerOrderItemAddOnSelectionCommand(rStr(), "+0");
        CustomerOrderItemAddOnSelectionCommand customerOrderItemAddOnSelection2 = new CustomerOrderItemAddOnSelectionCommand(rStr(), "+0");
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

    @Test
    public void releaseExpiredOrder() {
        ReflectionTestUtils.setField(orderApplicationService, "expireAfter", 1000L);
        Mockito.doReturn(new ArrayList<>()).when(orderRepository).findExpiredNotPaidReserved(any());
        orderApplicationService.releaseExpiredOrder();
        Mockito.verify(productService, times(0)).increaseOrderStorage(any(), anyString());
    }

    @Test
    public void releaseExpiredOrder_w_order_found() {
        ReflectionTestUtils.setField(orderApplicationService, "expireAfter", 1000L);
        ArrayList<CustomerOrder> customerOrders = new ArrayList<>();
        CustomerOrder customerOrder = getCustomerOrder();
        customerOrder.setOrderState(OrderState.NOT_PAID_RESERVED);
        customerOrders.add(customerOrder);
        Mockito.doReturn(customerOrders).when(orderRepository).findExpiredNotPaidReserved(any());
        orderApplicationService.releaseExpiredOrder();
        Mockito.verify(productService, times(1)).increaseOrderStorage(any(), anyString());
    }

    @Test
    public void resubmitOrder() {
        Mockito.doReturn(new ArrayList<>()).when(orderRepository).findPaidReserved();
        orderApplicationService.resubmitOrder();
        Mockito.verify(productService, times(0)).decreaseActualStorage(any(), anyString());
    }
}