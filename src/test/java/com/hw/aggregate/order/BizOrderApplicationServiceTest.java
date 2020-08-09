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

import static com.hw.aggregate.Helper.rLong;
import static com.hw.aggregate.Helper.rStr;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class BizOrderApplicationServiceTest {
    @InjectMocks
    BizOrderApplicationService orderApplicationService;
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
    BizOrderRepository orderRepository;
    @Mock
    CompletableFuture<Void> completableFuture;
    @Mock
    MessengerService messengerService;
    @Mock
    PlatformTransactionManager platformTransactionManager;
    @Mock
    EntityManager entityManager;

    private BizOrder getCustomerOrder() {
        BizOrder customerOrder = new BizOrder();
        List<BizOrderItem> customerOrderItems = new ArrayList<>();
        for (int a = 0; a < 5; a++) {
            customerOrderItems.add(getCustomerOrderItem());
        }
        customerOrder.setReadOnlyProductList(new ArrayList<>(customerOrderItems));
        customerOrder.setPaymentAmt(new BigDecimal(0));
        customerOrder.setId(1500L);

        return customerOrder;
    }

    private BizOrderItem getCustomerOrderItem() {
        BizOrderItem customerOrderItem = new BizOrderItem();

        customerOrderItem.setFinalPrice(BigDecimal.ZERO);
        customerOrderItem.setImageUrlSmall(rStr());
        customerOrderItem.setName(rStr());
        customerOrderItem.setProductId(rLong());

        List<BizOrderItemAddOn> customerOrderItemAddOns = new ArrayList<>();
        BizOrderItemAddOn customerOrderItemAddOn0 = new BizOrderItemAddOn();
        BizOrderItemAddOn customerOrderItemAddOn1 = new BizOrderItemAddOn();
        BizOrderItemAddOn customerOrderItemAddOn2 = new BizOrderItemAddOn();
        customerOrderItemAddOn0.setTitle(rStr());
        customerOrderItemAddOn1.setTitle(rStr());
        customerOrderItemAddOn2.setTitle(rStr());
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections0 = new ArrayList<>();
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections1 = new ArrayList<>();
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections2 = new ArrayList<>();
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection0 = new BizOrderItemAddOnSelection(rStr(), "+0");
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection1 = new BizOrderItemAddOnSelection(rStr(), "+0");
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection2 = new BizOrderItemAddOnSelection(rStr(), "+0");
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


}