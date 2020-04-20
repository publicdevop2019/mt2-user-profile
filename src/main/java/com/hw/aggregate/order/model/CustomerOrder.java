package com.hw.aggregate.order.model;

import com.hw.aggregate.order.exception.OrderPaymentMismatchException;
import com.hw.aggregate.order.exception.StateChangeException;
import com.hw.shared.Auditable;
import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "OrderDetail")
@SequenceGenerator(name = "orderId_gen", sequenceName = "orderId_gen", initialValue = 100)
@Data
public class CustomerOrder extends Auditable {
    /**
     * id setter is required to correctly work with BeanPropertyRowMapper for spring batch
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "orderId_gen")
    private Long id;

    @Column(name = "fk_profile")
    private Long profileId;
    /**
     * Address product all treat as embedded element instead of an entity
     */
    @NotNull
    @Valid
    @Embedded
    private CustomerOrderAddress address;

    @Column(length = 100000)
    private ArrayList<CustomerOrderItem> readOnlyProductList;

    @Column
    @ElementCollection
    @CollectionTable(name = "order_product_snapshot", joinColumns = @JoinColumn(name = "order_id"))
    private List<CustomerOrderItem> writeOnlyProductList;

    @NotNull
    @NotEmpty
    private String paymentType;

    @Column
    private String paymentLink;

    @Column
    @NotNull
    private BigDecimal paymentAmt;

    @Column
    private String paymentDate;

    @Column
    @Getter
    private OrderState orderState;

    @Column
    private Date modifiedByUserAt;

    @Version
    private Integer version;

    public CustomerOrder() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerOrder that = (CustomerOrder) o;
        return
                Objects.equals(id, that.id) &&
                        Objects.equals(address, that.address) &&
                        /**
                         * use deepEquals for JPA persistentBag workaround, otherwise equals will return incorrect result
                         */
                        Objects.deepEquals(readOnlyProductList.toArray(), that.readOnlyProductList.toArray()) &&
                        Objects.equals(paymentType, that.paymentType) &&
                        Objects.equals(profileId, that.profileId) &&
                        Objects.equals(paymentAmt, that.paymentAmt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, readOnlyProductList, paymentType, paymentAmt, profileId);
    }

    public void updateModifiedByUserAt() {
        this.modifiedByUserAt = Date.from(Instant.now());
    }

    public void setAddress(CustomerOrderAddress address) {
        this.address = new CustomerOrderAddress(address.getFullName(), address.getLine1(), address.getLine2()
                , address.getPostalCode(), address.getPhoneNumber(), address.getCity(), address.getProvince(), address.getCountry());
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public static CustomerOrder create(Long profileId, List<CustomerOrderItem> productList, CustomerOrderAddress address, String paymentType, BigDecimal paymentAmt) {
        return new CustomerOrder(profileId, productList, address, paymentType, paymentAmt);
    }

    private CustomerOrder(Long profileId, List<CustomerOrderItem> productList, CustomerOrderAddress address, String paymentType, BigDecimal paymentAmt) {
        this.profileId = profileId;
        this.readOnlyProductList = new ArrayList<>(productList);
        this.writeOnlyProductList = productList;
        this.address = address;
        this.paymentType = paymentType;
        this.paymentAmt = paymentAmt;
        this.modifiedByUserAt = Date.from(Instant.now());
        validatePaymentAmount();
        toNotPaidReserved();
    }

    /**
     * merge multiple same product into one if possible
     *
     * @return
     */
    public Map<String, Integer> getProductSummary() {
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        readOnlyProductList.forEach(e -> {
            int defaultAmount = 1;
            if (e.getSelectedOptions() != null) {
                Optional<CustomerOrderItemAddOn> qty = e.getSelectedOptions().stream().filter(el -> el.title.equals("qty")).findFirst();
                if (qty.isPresent() && !qty.get().options.isEmpty()) {
                    /**
                     * deduct amount based on qty value, otherwise default is 1
                     */
                    defaultAmount = Integer.parseInt(qty.get().options.get(0).optionValue);
                }
            }
            if (stringIntegerHashMap.containsKey(e.getProductId())) {
                stringIntegerHashMap.put(e.getProductId(), stringIntegerHashMap.get(e.getProductId()) + defaultAmount);
            } else {
                stringIntegerHashMap.put(e.getProductId(), defaultAmount);
            }
        });
        return stringIntegerHashMap;
    }

    public void toNotPaidReserved() {
        // new order or recycled order
        if ((id == null && orderState == null) || id != null && orderState.equals(OrderState.NOT_PAID_RECYCLED)) {
            orderState = OrderState.NOT_PAID_RESERVED;
        } else {
            throw new StateChangeException();
        }
    }

    public void toNotPaidRecycled() {
        if (orderState != OrderState.NOT_PAID_RESERVED)
            throw new StateChangeException();
        orderState = OrderState.NOT_PAID_RECYCLED;
    }

    public void toPaidReserved() {
        if (orderState == OrderState.NOT_PAID_RESERVED || orderState == OrderState.PAID_RECYCLED) {
            orderState = OrderState.PAID_RESERVED;
        } else {
            throw new StateChangeException();
        }
    }

    public void toConfirmed() {
        if (orderState != OrderState.PAID_RESERVED)
            throw new StateChangeException();
        orderState = OrderState.CONFIRMED;
    }

    public void toPaidRecycled() {
        if (orderState != OrderState.NOT_PAID_RECYCLED)
            throw new StateChangeException();
        orderState = OrderState.PAID_RECYCLED;
    }

    private void validatePaymentAmount() {
        BigDecimal reduce = readOnlyProductList.stream().map(e -> BigDecimal.valueOf(Double.parseDouble(e.getFinalPrice()))).reduce(BigDecimal.valueOf(0), BigDecimal::add);
        if (paymentAmt.compareTo(reduce) != 0)
            throw new OrderPaymentMismatchException();
    }
}

