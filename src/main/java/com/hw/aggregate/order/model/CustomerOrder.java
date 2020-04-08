package com.hw.aggregate.order.model;

import com.hw.aggregate.order.exception.OrderAlreadyPaidException;
import com.hw.aggregate.order.exception.OrderPaymentMismatchException;
import com.hw.shared.Auditable;
import lombok.Data;

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


    @ElementCollection
    @CollectionTable(name = "order_product_snapshot", joinColumns = @JoinColumn(name = "order_id"))
    @Column
    private List<CustomerOrderItem> productList;

    @NotNull
    @NotEmpty
    private String paymentType;

    @Column
    @NotNull
    private BigDecimal paymentAmt;

    @Column
    private String paymentDate;


    @Column
    private CustomerOrderPaymentStatus paymentStatus;

    @Column
    private Date modifiedByUserAt;

    @Column
    private Boolean expired;

    @Column
    private Boolean revoked;

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
                        Objects.deepEquals(productList.toArray(), that.productList.toArray()) &&
                        Objects.equals(paymentType, that.paymentType) &&
                        Objects.equals(paymentAmt, that.paymentAmt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, productList, paymentType, paymentAmt);
    }

    public void updateModifiedByUserAt() {
        if (paymentStatus != null && paymentStatus.equals(CustomerOrderPaymentStatus.paid))
            throw new OrderAlreadyPaidException();
        this.modifiedByUserAt = Date.from(Instant.now());
    }

    public void setAddress(CustomerOrderAddress address) {
        if (paymentStatus != null && paymentStatus.equals(CustomerOrderPaymentStatus.paid))
            throw new OrderAlreadyPaidException();
        this.address = new CustomerOrderAddress(address.getFullName(), address.getLine1(), address.getLine2()
                , address.getPostalCode(), address.getPhoneNumber(), address.getCity(), address.getProvince(), address.getCountry());
    }

    public void setPaymentType(String paymentType) {
        if (paymentStatus != null && paymentStatus.equals(CustomerOrderPaymentStatus.paid))
            throw new OrderAlreadyPaidException();
        this.paymentType = paymentType;
    }

    /**
     * update payment status, ex through if order is already paid
     *
     * @param paymentStatus
     */
    public void setPaymentStatus(Boolean paymentStatus) {
        if (paymentStatus && this.paymentStatus.equals(CustomerOrderPaymentStatus.paid))
            throw new OrderAlreadyPaidException();
        this.paymentStatus = paymentStatus ? CustomerOrderPaymentStatus.paid : CustomerOrderPaymentStatus.unpaid;
    }

    public static CustomerOrder create(Long profileId, List<CustomerOrderItem> productList, CustomerOrderAddress address, String paymentType, BigDecimal paymentAmt) {
        return new CustomerOrder(profileId, productList, address, paymentType, paymentAmt);
    }

    private CustomerOrder(Long profileId, List<CustomerOrderItem> productList, CustomerOrderAddress address, String paymentType, BigDecimal paymentAmt) {
//        this.profileId = profileId;
        this.productList = productList;
        this.address = address;
        this.paymentType = paymentType;
        this.paymentAmt = paymentAmt;
        this.paymentStatus = CustomerOrderPaymentStatus.unpaid;
        this.expired = Boolean.FALSE;
        this.revoked = Boolean.FALSE;
        this.modifiedByUserAt = Date.from(Instant.now());
    }

    /**
     * merge multiple same product into one if possible
     *
     * @return
     */
    public Map<String, Integer> getProductSummary() {
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        productList.forEach(e -> {
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

    public void validatePaymentAmount() {
        BigDecimal reduce = productList.stream().map(e -> BigDecimal.valueOf(Double.parseDouble(e.getFinalPrice()))).reduce(BigDecimal.valueOf(0), BigDecimal::add);
        if (paymentAmt.compareTo(reduce) != 0)
            throw new OrderPaymentMismatchException();
    }
}

