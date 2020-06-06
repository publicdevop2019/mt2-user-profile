//package com.hw.aggregate.order.model;
//
//import com.hw.aggregate.order.exception.StateChangeException;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class OrderEventTest {
//
//    @Test
//    public void nextState() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.NOT_PAID_RESERVED);
//        OrderEvent.CONFIRM_PAYMENT.nextState(customerOrder);
//        Assert.assertEquals(OrderState.PAID_RESERVED, customerOrder.getOrderState());
//    }
//
//    @Test
//    public void nextState2() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.NOT_PAID_RECYCLED);
//        OrderEvent.CONFIRM_PAYMENT.nextState(customerOrder);
//        Assert.assertEquals(OrderState.PAID_RECYCLED, customerOrder.getOrderState());
//    }
//
//    @Test(expected = StateChangeException.class)
//    public void nextState3() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.PAID_RECYCLED);
//        OrderEvent.CONFIRM_PAYMENT.nextState(customerOrder);
//    }
//
//    @Test
//    public void nextState4() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.NOT_PAID_RESERVED);
//        OrderEvent.RECYCLE_ORDER_STORAGE.nextState(customerOrder);
//        Assert.assertEquals(OrderState.NOT_PAID_RECYCLED, customerOrder.getOrderState());
//    }
//
//    @Test(expected = StateChangeException.class)
//    public void nextState5() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.PAID_RESERVED);
//        OrderEvent.RECYCLE_ORDER_STORAGE.nextState(customerOrder);
//    }
//
//    @Test
//    public void nextState6() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.PAID_RESERVED);
//        OrderEvent.DECREASE_ACTUAL_STORAGE.nextState(customerOrder);
//        Assert.assertEquals(OrderState.CONFIRMED, customerOrder.getOrderState());
//    }
//
//    @Test(expected = StateChangeException.class)
//    public void nextState7() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.NOT_PAID_RESERVED);
//        OrderEvent.DECREASE_ACTUAL_STORAGE.nextState(customerOrder);
//    }
//
//    @Test
//    public void nextState8() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.PAID_RECYCLED);
//        OrderEvent.RESERVE.nextState(customerOrder);
//        Assert.assertEquals(OrderState.PAID_RESERVED, customerOrder.getOrderState());
//    }
//
//    @Test
//    public void nextState9() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.NOT_PAID_RECYCLED);
//        OrderEvent.RESERVE.nextState(customerOrder);
//        Assert.assertEquals(OrderState.NOT_PAID_RESERVED, customerOrder.getOrderState());
//    }
//
//    @Test(expected = StateChangeException.class)
//    public void nextState10() {
//        CustomerOrder customerOrder = new CustomerOrder();
//        customerOrder.setOrderState(OrderState.NOT_PAID_RESERVED);
//        OrderEvent.RESERVE.nextState(customerOrder);
//    }
//}