package com.hw.aggregate.order.model;

import com.hw.aggregate.order.BizOrderRepository;
import com.hw.aggregate.order.command.PlaceBizOrderAgainCommand;
import com.hw.aggregate.order.exception.BizOrderAccessException;
import com.hw.aggregate.order.exception.BizOrderNotExistException;
import com.hw.aggregate.order.exception.BizOrderPaymentMismatchException;
import com.hw.shared.Auditable;
import com.hw.shared.PatchCommand;
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

import static com.hw.shared.AppConstant.PATCH_OP_TYPE_DIFF;
import static com.hw.shared.AppConstant.PATCH_OP_TYPE_SUM;

@Entity
@Table(name = "OrderDetail")
@Data
@NoArgsConstructor
public class BizOrder extends Auditable {
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
    private BizOrderAddress address;

    @Column(length = 100000)
    private ArrayList<BizOrderItem> readOnlyProductList;

    @Column
    @ElementCollection
    @CollectionTable(name = "order_product_snapshot", joinColumns = @JoinColumn(name = "order_id"))
    private List<BizOrderItem> writeOnlyProductList;

    @NotEmpty
    @Column(nullable = false)
    private String paymentType;

    private String paymentLink;

    @NotNull
    @Column(nullable = false)
    private BigDecimal paymentAmt;

    private String paymentDate;

    @Convert(converter = MapConverter.class)
    private Map<BizOrderEvent, String> transactionHistory;

    @NotNull
    @Column
    private Boolean paid;

    @Getter
    @Column(length = 25)
    @Convert(converter = BizOrderStatus.DBConverter.class)
    private BizOrderStatus orderState;

    @NotNull
    @Column(nullable = false)
    private Date modifiedByUserAt;

    @Version
    private Integer version;

    public void updateModifiedByUserAt() {
        this.modifiedByUserAt = Date.from(Instant.now());
    }

    public static BizOrder create(Long id, Long profileId, List<BizOrderItemCommand> productList, BizOrderAddressCmdRep address, String paymentType, BigDecimal paymentAmt) {
        return new BizOrder(id, profileId, productList, address, paymentType, paymentAmt);
    }

    private BizOrder(Long id, Long profileId, List<BizOrderItemCommand> productList, BizOrderAddressCmdRep address, String paymentType, BigDecimal paymentAmt) {
        List<BizOrderItem> collect2 = productList.stream().map(e -> {
            BizOrderItem customerOrderItem = new BizOrderItem();
            customerOrderItem.setFinalPrice(e.getFinalPrice());
            customerOrderItem.setProductId(e.getProductId());
            customerOrderItem.setName(e.getName());
            customerOrderItem.setImageUrlSmall(e.getImageUrlSmall());
            customerOrderItem.setAttributesSales(e.getAttributesSales());
            if (e.getAttrIdMap() != null)
                customerOrderItem.setAttrIdMap(new HashMap<>(e.getAttrIdMap()));
            List<BizOrderItemAddOn> collect1 = null;
            if (e.getSelectedOptions() != null) {
                collect1 = e.getSelectedOptions().stream().map(e2 -> {
                    BizOrderItemAddOn customerOrderItemAddOn = new BizOrderItemAddOn();
                    customerOrderItemAddOn.setTitle(e2.getTitle());
                    List<BizOrderItemAddOnSelection> collect = e2.getOptions().stream()
                            .map(e3 -> new BizOrderItemAddOnSelection(e3.getOptionValue(), e3.getPriceVar())).collect(Collectors.toList());
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
        BizOrderAddress customerOrderAddress = new BizOrderAddress();
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
        this.orderState = BizOrderStatus.DRAFT;
        this.paid = false;
    }

    public List<PatchCommand> getReserveOrderPatchCommands() {
        List<PatchCommand> details = new ArrayList<>();
        readOnlyProductList.forEach(e -> {
            int amount = 1;
            if (e.getSelectedOptions() != null) {
                Optional<BizOrderItemAddOn> qty = e.getSelectedOptions().stream().filter(el -> el.getTitle().equals("qty")).findFirst();
                if (qty.isPresent() && !qty.get().getOptions().isEmpty()) {
                    /**
                     * deduct amount based on qty value, otherwise default is 1
                     */
                    amount = Integer.parseInt(qty.get().getOptions().get(0).getOptionValue());
                }
            }
            PatchCommand patchCommand = new PatchCommand();
            patchCommand.setOp(PATCH_OP_TYPE_DIFF);
            patchCommand.setValue(String.valueOf(amount));
            patchCommand.setPath(getPatchPath(e, "storageOrder"));
            details.add(patchCommand);
        });
        return details;
    }

    private String getPatchPath(BizOrderItem e, String fieldName) {
        String replace = String.join(",", e.getAttributesSales()).replace(":", "-").replace("/", "~/");
        return "/" + e.getProductId() + "/skus?query=attributesSales:" + replace + "/" + fieldName;
    }

    public List<PatchCommand> getConfirmOrderPatchCommands() {
        List<PatchCommand> details = new ArrayList<>();
        readOnlyProductList.forEach(e -> {
            int amount = 1;
            if (e.getSelectedOptions() != null) {
                Optional<BizOrderItemAddOn> qty = e.getSelectedOptions().stream().filter(el -> el.getTitle().equals("qty")).findFirst();
                if (qty.isPresent() && !qty.get().getOptions().isEmpty()) {
                    /**
                     * deduct amount based on qty value, otherwise default is 1
                     */
                    amount = Integer.parseInt(qty.get().getOptions().get(0).getOptionValue());
                }
            }
            PatchCommand storageActualCmd = new PatchCommand();
            storageActualCmd.setOp(PATCH_OP_TYPE_DIFF);
            storageActualCmd.setValue(String.valueOf(amount));
            storageActualCmd.setPath(getPatchPath(e, "storageActual"));
            PatchCommand salesCmd = new PatchCommand();
            salesCmd.setOp(PATCH_OP_TYPE_SUM);
            salesCmd.setValue(String.valueOf(amount));
            salesCmd.setPath(getPatchPath(e, "sales"));
            PatchCommand totalSalesCmd = new PatchCommand();
            totalSalesCmd.setOp(PATCH_OP_TYPE_SUM);
            totalSalesCmd.setValue(String.valueOf(amount));
            totalSalesCmd.setPath("/" + e.getProductId() + "/" + "totalSales");
            details.add(totalSalesCmd);
            details.add(storageActualCmd);
            details.add(salesCmd);
        });
        return details;
    }

    private void validatePaymentAmount() {
        BigDecimal reduce = readOnlyProductList.stream().map(BizOrderItem::getFinalPrice).reduce(BigDecimal.valueOf(0), BigDecimal::add);
        if (paymentAmt.compareTo(reduce) != 0)
            throw new BizOrderPaymentMismatchException();
    }

    public static BizOrder get(Long profileId, Long orderId, BizOrderRepository orderRepository) {
        Optional<BizOrder> byId = orderRepository.findById(orderId);
        checkAccess(byId, profileId);
        return byId.get();
    }

    public static BizOrder getWOptLock(Long profileId, Long orderId, BizOrderRepository orderRepository) {
        Optional<BizOrder> byId = orderRepository.findByIdOptLock(orderId);
        checkAccess(byId, profileId);
        return byId.get();
    }

    private static void checkAccess(Optional<BizOrder> byId, Long profileId) {
        if (byId.isEmpty())
            throw new BizOrderNotExistException();
        if (!byId.get().getProfileId().equals(profileId))
            throw new BizOrderAccessException();
    }

    public void updateAddress(PlaceBizOrderAgainCommand command) {
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

