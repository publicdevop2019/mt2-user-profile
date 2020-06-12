package com.hw.aggregate.order;

import com.hw.aggregate.cart.CartApplicationService;
import com.hw.aggregate.order.model.*;
import com.hw.shared.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.hw.aggregate.Helper.rStr;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class OrderApplicationServiceTest {
    @InjectMocks
    OrderApplicationService orderApplicationService;
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
    CustomerOrderRepository orderRepository;
    @Mock
    CompletableFuture<Void> completableFuture;
    @Mock
    MessengerService messengerService;
    @Mock
    PlatformTransactionManager platformTransactionManager;
    @Mock
    EntityManager entityManager;

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