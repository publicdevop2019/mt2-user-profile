package com.hw.aggregate.order.model;

import com.hw.aggregate.order.BizOrderRepository;
import com.hw.aggregate.order.SagaOrchestratorService;
import com.hw.aggregate.order.command.UserCreateBizOrderCommand;
import com.hw.aggregate.order.command.UserPlaceBizOrderAgainCommand;
import com.hw.aggregate.order.exception.BizOrderPaymentMismatchException;
import com.hw.aggregate.order.model.product.AppProductOption;
import com.hw.aggregate.order.model.product.AppProductSku;
import com.hw.aggregate.order.model.product.AppProductSumPagedRep;
import com.hw.shared.Auditable;
import com.hw.shared.UserThreadLocal;
import com.hw.shared.rest.IdBasedEntity;
import com.hw.shared.rest.exception.EntityNotExistException;
import com.hw.shared.sql.PatchCommand;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hw.shared.AppConstant.PATCH_OP_TYPE_DIFF;
import static com.hw.shared.AppConstant.PATCH_OP_TYPE_SUM;

@Entity
@Table(name = "biz_order")
@Data
@NoArgsConstructor
@Slf4j
public class BizOrder extends Auditable implements IdBasedEntity {
    /**
     * id setter is required to correctly work with BeanPropertyRowMapper for spring batch
     */
    @Id
    private Long id;

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
    @CollectionTable(name = "biz_order_product_snapshot", joinColumns = @JoinColumn(name = "order_id"))
    private List<BizOrderItem> writeOnlyProductList;

    @NotEmpty
    @Column(nullable = false)
    private String paymentType;

    private String paymentLink;

    @NotNull
    @Column(nullable = false)
    private BigDecimal paymentAmt;

    private String paymentDate;

    @NotNull
    @Column
    private Boolean paid;

    @Getter
    @Column(length = 25)
    @Convert(converter = BizOrderStatus.DBConverter.class)
    private BizOrderStatus orderState;
    public static final String ENTITY_ORDER_ORDER_STATE = "orderState";

    @NotNull
    @Column(nullable = false)
    private Date modifiedByUserAt;

    @Version
    private Integer version;

    public static BizOrder create(long id, UserCreateBizOrderCommand command, SagaOrchestratorService sagaOrchestratorService, String changeId) {
        log.debug("start of createNew");
        BizOrder customerOrder = new BizOrder(id, command);
        SagaOrchestratorService.CreateBizStateMachineCommand createBizStateMachineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
        createBizStateMachineCommand.setOrderId(id);
        createBizStateMachineCommand.setBizOrderEvent(BizOrderEvent.NEW_ORDER);
        createBizStateMachineCommand.setCreatedBy(UserThreadLocal.get());
        createBizStateMachineCommand.setUserId(Long.parseLong(UserThreadLocal.get()));
        createBizStateMachineCommand.setOrderState(customerOrder.orderState);
        createBizStateMachineCommand.setPrepareEvent(BizOrderEvent.PREPARE_NEW_ORDER);
        createBizStateMachineCommand.setOrderStorageChange(customerOrder.getReserveOrderPatchCommands());
        createBizStateMachineCommand.setActualStorageChange(customerOrder.getConfirmOrderPatchCommands());
        createBizStateMachineCommand.setTxId(changeId);
        SagaOrchestratorService.BizStateMachineRep start = sagaOrchestratorService.start(createBizStateMachineCommand);
        customerOrder.setPaymentLink(start.getPaymentLink());
        customerOrder.setOrderState(start.getOrderState());
        return customerOrder;
    }

    public void updateModifiedByUserAt() {
        this.modifiedByUserAt = Date.from(Instant.now());
    }

    private BizOrder(Long id, UserCreateBizOrderCommand command) {
        List<BizOrderItem> collect2 = command.getProductList().stream().map(e -> {
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
        this.paymentAmt = command.getPaymentAmt();
        validatePaymentAmount();
        this.id = id;
        this.writeOnlyProductList = collect2;
        BizOrderAddress customerOrderAddress = new BizOrderAddress();
        customerOrderAddress.setOrderAddressCity(command.getAddress().getCity());
        customerOrderAddress.setOrderAddressCountry(command.getAddress().getCountry());
        customerOrderAddress.setOrderAddressFullName(command.getAddress().getFullName());
        customerOrderAddress.setOrderAddressLine1(command.getAddress().getLine1());
        customerOrderAddress.setOrderAddressLine2(command.getAddress().getLine2());
        customerOrderAddress.setOrderAddressPhoneNumber(command.getAddress().getPhoneNumber());
        customerOrderAddress.setOrderAddressProvince(command.getAddress().getProvince());
        customerOrderAddress.setOrderAddressPostalCode(command.getAddress().getPostalCode());
        this.address = customerOrderAddress;
        this.paymentType = command.getPaymentType();
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
            patchCommand.setExpect(1);
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
            storageActualCmd.setExpect(1);
            PatchCommand salesCmd = new PatchCommand();
            salesCmd.setOp(PATCH_OP_TYPE_SUM);
            salesCmd.setValue(String.valueOf(amount));
            salesCmd.setPath(getPatchPath(e, "sales"));
            salesCmd.setExpect(1);
            PatchCommand totalSalesCmd = new PatchCommand();
            totalSalesCmd.setOp(PATCH_OP_TYPE_SUM);
            totalSalesCmd.setValue(String.valueOf(amount));
            totalSalesCmd.setPath("/" + e.getProductId() + "/" + "totalSales");
            totalSalesCmd.setExpect(1);
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

    public static BizOrder getWOptLock(Long id, String userId, BizOrderRepository orderRepository) {
        Optional<BizOrder> byId = orderRepository.findByIdOptLock(id, userId);
        if (byId.isEmpty())
            throw new EntityNotExistException();
        return byId.get();
    }

    public void updateAddress(UserPlaceBizOrderAgainCommand command) {
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

    public void confirmPayment(SagaOrchestratorService sagaOrchestratorService, String changeId) {
        log.debug("start of confirmPayment {}", id);
        SagaOrchestratorService.CreateBizStateMachineCommand createBizStateMachineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
        createBizStateMachineCommand.setOrderId(id);
        createBizStateMachineCommand.setBizOrderEvent(BizOrderEvent.CONFIRM_PAYMENT);
        createBizStateMachineCommand.setPrepareEvent(BizOrderEvent.PREPARE_CONFIRM_PAYMENT);
        createBizStateMachineCommand.setCreatedBy(UserThreadLocal.get());
        createBizStateMachineCommand.setUserId(Long.parseLong(UserThreadLocal.get()));
        createBizStateMachineCommand.setOrderState(this.orderState);
        createBizStateMachineCommand.setTxId(changeId);
        SagaOrchestratorService.BizStateMachineRep start = sagaOrchestratorService.start(createBizStateMachineCommand);
        this.setOrderState(start.getOrderState());
        this.setPaid(start.getPaymentStatus());
    }

    public void reserve(SagaOrchestratorService sagaOrchestratorService, String changeId) {
        log.info("reserve order {} again", id);
        log.info("order status {}", this.getOrderState());
        SagaOrchestratorService.CreateBizStateMachineCommand createBizStateMachineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
        createBizStateMachineCommand.setOrderId(id);
        createBizStateMachineCommand.setBizOrderEvent(BizOrderEvent.RESERVE);
        createBizStateMachineCommand.setPrepareEvent(BizOrderEvent.PREPARE_RESERVE);
        createBizStateMachineCommand.setCreatedBy(UserThreadLocal.get());
        createBizStateMachineCommand.setUserId(Long.parseLong(UserThreadLocal.get()));
        createBizStateMachineCommand.setOrderState(this.orderState);
        createBizStateMachineCommand.setTxId(changeId);
        SagaOrchestratorService.BizStateMachineRep start = sagaOrchestratorService.start(createBizStateMachineCommand);
        this.setOrderState(start.getOrderState());
        this.setPaymentLink(start.getPaymentLink());
    }
    public boolean validateProducts(AppProductSumPagedRep appProductSumPagedRep) {
        return !this.getReadOnlyProductList().stream().anyMatch(command -> {
            Optional<AppProductSumPagedRep.ProductAdminCardRepresentation> byId = appProductSumPagedRep.getData().stream().filter(e -> e.getId().equals(command.getProductId())).findFirst();
            //validate product match
            if (byId.isEmpty())
                return true;
            BigDecimal price;
            List<AppProductSku> collect = byId.get().getProductSkuList().stream().filter(productSku -> new TreeSet(productSku.getAttributesSales()).equals(new TreeSet(command.getAttributesSales()))).collect(Collectors.toList());
            price = collect.get(0).getPrice();
            //if no option present then compare final price
            if (command.getSelectedOptions() == null || command.getSelectedOptions().size() == 0) {
                return price.compareTo(command.getFinalPrice()) != 0;
            }
            //validate product option match
            List<AppProductOption> storedOption = byId.get().getSelectedOptions();
            if (storedOption == null || storedOption.size() == 0)
                return true;
            boolean optionAllMatch = command.getSelectedOptions().stream().allMatch(userSelected -> {
                //check selected option is valid option
                Optional<AppProductOption> first = storedOption.stream().filter(storedOptionItem -> {
                    // compare title
                    if (!storedOptionItem.title.equals(userSelected.getTitle()))
                        return false;
                    //compare option value for each title
                    String optionValue = userSelected.getOptions().get(0).getOptionValue();
                    Optional<AppProductOption.OptionItem> first1 = storedOptionItem.options.stream().filter(optionItem -> optionItem.getOptionValue().equals(optionValue)).findFirst();
                    if (first1.isEmpty())
                        return false;
                    return true;
                }).findFirst();
                if (first.isEmpty())
                    return false;
                else {
                    return true;
                }
            });
            if (!optionAllMatch)
                return true;
            //validate product final price
            BigDecimal finalPrice = command.getFinalPrice();
            // get all price variable
            List<String> userSelectedAddOnTitles = command.getSelectedOptions().stream().map(BizOrderItemAddOn::getTitle).collect(Collectors.toList());
            // filter option based on title
            Stream<AppProductOption> storedAddonMatchingUserSelection = byId.get().getSelectedOptions().stream().filter(var1 -> userSelectedAddOnTitles.contains(var1.getTitle()));
            // map to value detail for each title
            List<String> priceVarCollection = storedAddonMatchingUserSelection.map(storedMatchAddon -> {
                String title = storedMatchAddon.getTitle();
                //find right option for title
                Optional<BizOrderItemAddOn> user_addon_option = command.getSelectedOptions().stream().filter(e -> e.getTitle().equals(title)).findFirst();
                BizOrderItemAddOnSelection user_optionItem = user_addon_option.get().getOptions().get(0);
                Optional<AppProductOption.OptionItem> first = storedMatchAddon.getOptions().stream().filter(db_optionItem -> db_optionItem.getOptionValue().equals(user_optionItem.getOptionValue())).findFirst();
                return first.get().getPriceVar();
            }).collect(Collectors.toList());
            for (String priceVar : priceVarCollection) {
                if (priceVar.contains("+")) {
                    double v = Double.parseDouble(priceVar.replace("+", ""));
                    BigDecimal bigDecimal = BigDecimal.valueOf(v);
                    price = price.add(bigDecimal);
                } else if (priceVar.contains("-")) {
                    double v = Double.parseDouble(priceVar.replace("-", ""));
                    BigDecimal bigDecimal = BigDecimal.valueOf(v);
                    price = price.subtract(bigDecimal);

                } else if (priceVar.contains("*")) {
                    double v = Double.parseDouble(priceVar.replace("*", ""));
                    BigDecimal bigDecimal = BigDecimal.valueOf(v);
                    price = price.multiply(bigDecimal);
                } else {
                    log.error("unknown operation type");
                }
            }
            if (price.compareTo(finalPrice) == 0) {
                log.debug("value does match for product {}, expected {} actual {}", command.getProductId(), price, finalPrice);
                return false;
            }
            return true;
        });
    }
}

