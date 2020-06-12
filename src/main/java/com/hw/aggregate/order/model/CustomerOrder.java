package com.hw.aggregate.order.model;

import com.hw.aggregate.order.CustomerOrderRepository;
import com.hw.aggregate.order.command.PlaceOrderAgainCommand;
import com.hw.aggregate.order.exception.OrderAccessException;
import com.hw.aggregate.order.exception.OrderNotExistException;
import com.hw.aggregate.order.exception.OrderPaymentMismatchException;
import com.hw.config.TransactionIdGenerator;
import com.hw.shared.Auditable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "OrderDetail")
@Data
@NoArgsConstructor
public class CustomerOrder extends Auditable {
    /**
     * id setter is required to correctly work with BeanPropertyRowMapper for spring batch
     */
    @Id
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

    @NotEmpty
    @Column(nullable = false)
    private String paymentType;

    private String paymentLink;

    @NotNull
    @Column(nullable = false)
    private BigDecimal paymentAmt;

    private String paymentDate;

    private String currentTransactionId;

    private String nextTransactionId;

    @Convert(converter = MapConverter.class)
    private Map<OrderState, String> transactionHistory;

    @NotNull
    @Column
    private Boolean paid;

    @Getter
    @Column(length = 25)
    @Convert(converter = OrderState.DBConverter.class)
    private OrderState orderState;

    @NotNull
    @Column(nullable = false)
    private Date modifiedByUserAt;

    @Version
    private Integer version;

    public void updateModifiedByUserAt() {
        this.modifiedByUserAt = Date.from(Instant.now());
    }

    public static CustomerOrder create(Long id, Long profileId, List<CustomerOrderItemCommand> productList, CustomerOrderAddressCmdRep address, String paymentType, BigDecimal paymentAmt) {
        return new CustomerOrder(id, profileId, productList, address, paymentType, paymentAmt);
    }

    private CustomerOrder(Long id, Long profileId, List<CustomerOrderItemCommand> productList, CustomerOrderAddressCmdRep address, String paymentType, BigDecimal paymentAmt) {
        List<CustomerOrderItem> collect2 = productList.stream().map(e -> {
            CustomerOrderItem customerOrderItem = new CustomerOrderItem();
            customerOrderItem.setFinalPrice(e.getFinalPrice());
            customerOrderItem.setProductId(e.getProductId());
            customerOrderItem.setName(e.getName());
            customerOrderItem.setImageUrlSmall(e.getImageUrlSmall());
            List<CustomerOrderItemAddOn> collect1 = null;
            if (e.getSelectedOptions() != null) {
                collect1 = e.getSelectedOptions().stream().map(e2 -> {
                    CustomerOrderItemAddOn customerOrderItemAddOn = new CustomerOrderItemAddOn();
                    customerOrderItemAddOn.setTitle(e2.getTitle());
                    List<CustomerOrderItemAddOnSelection> collect = e2.getOptions().stream()
                            .map(e3 -> new CustomerOrderItemAddOnSelection(e3.getOptionValue(), e3.getPriceVar())).collect(Collectors.toList());
                    customerOrderItemAddOn.setOptions(collect);
                    return customerOrderItemAddOn;
                }).collect(Collectors.toList());
            }
            customerOrderItem.setSelectedOptions(collect1);
            return customerOrderItem;
        }).collect(Collectors.toList());
        this.readOnlyProductList = new ArrayList<>(collect2);
        this.paymentAmt = paymentAmt;
        validatePaymentAmount();
        this.id = id;
        this.profileId = profileId;
        this.writeOnlyProductList = collect2;
        CustomerOrderAddress customerOrderAddress = new CustomerOrderAddress();
        customerOrderAddress.setOrderAddressCity(address.getCity());
        customerOrderAddress.setOrderAddressCountry(address.getCountry());
        customerOrderAddress.setOrderAddressFullName(address.getFullName());
        customerOrderAddress.setOrderAddressLine1(address.getLine1());
        customerOrderAddress.setOrderAddressLine2(address.getLine2());
        customerOrderAddress.setOrderAddressPhoneNumber(address.getPhoneNumber());
        customerOrderAddress.setOrderAddressProvince(address.getProvince());
        customerOrderAddress.setOrderAddressPostalCode(address.getPostalCode());
        this.address = customerOrderAddress;
        this.paymentType = paymentType;
        this.modifiedByUserAt = Date.from(Instant.now());
        this.orderState = OrderState.DRAFT;
        this.paid = false;
        this.currentTransactionId = TransactionIdGenerator.getId();
        this.nextTransactionId = TransactionIdGenerator.getId();
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
                Optional<CustomerOrderItemAddOn> qty = e.getSelectedOptions().stream().filter(el -> el.getTitle().equals("qty")).findFirst();
                if (qty.isPresent() && !qty.get().getOptions().isEmpty()) {
                    /**
                     * deduct amount based on qty value, otherwise default is 1
                     */
                    defaultAmount = Integer.parseInt(qty.get().getOptions().get(0).getOptionValue());
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

    private void validatePaymentAmount() {
        BigDecimal reduce = readOnlyProductList.stream().map(e -> BigDecimal.valueOf(Double.parseDouble(e.getFinalPrice()))).reduce(BigDecimal.valueOf(0), BigDecimal::add);
        if (paymentAmt.compareTo(reduce) != 0)
            throw new OrderPaymentMismatchException();
    }

    public static CustomerOrder get(Long profileId, Long orderId, CustomerOrderRepository orderRepository) {
        Optional<CustomerOrder> byId = orderRepository.findById(orderId);
        checkAccess(byId, profileId);
        return byId.get();
    }

    public static CustomerOrder getForUpdate(Long profileId, Long orderId, CustomerOrderRepository orderRepository) {
        Optional<CustomerOrder> byId = orderRepository.findByIdOptLock(orderId);
        checkAccess(byId, profileId);
        return byId.get();
    }

    private static void checkAccess(Optional<CustomerOrder> byId, Long profileId) {
        if (byId.isEmpty())
            throw new OrderNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new OrderAccessException();
    }

    public void updateAddress(PlaceOrderAgainCommand command) {
        if (command.getAddress() != null
                && StringUtils.hasText(command.getAddress().getCountry())
                && StringUtils.hasText(command.getAddress().getProvince())
                && StringUtils.hasText(command.getAddress().getCity())
                && StringUtils.hasText(command.getAddress().getPostalCode())
                && StringUtils.hasText(command.getAddress().getLine1())
                && StringUtils.hasText(command.getAddress().getFullName())
                && StringUtils.hasText(command.getAddress().getPhoneNumber())
        ) {
            address.setOrderAddressCountry(command.getAddress().getCountry());
            address.setOrderAddressProvince(command.getAddress().getProvince());
            address.setOrderAddressCity(command.getAddress().getCity());
            address.setOrderAddressPostalCode(command.getAddress().getPostalCode());
            address.setOrderAddressLine1(command.getAddress().getLine1());
            address.setOrderAddressLine2(command.getAddress().getLine2());
            address.setOrderAddressFullName(command.getAddress().getFullName());
            address.setOrderAddressPhoneNumber(command.getAddress().getPhoneNumber());
        }
        updateModifiedByUserAt();
    }
}

