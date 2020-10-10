package com.hw.aggregate.order.model;

import com.hw.aggregate.order.BizOrderRepository;
import com.hw.aggregate.order.SagaOrchestratorService;
import com.hw.aggregate.order.command.*;
import com.hw.aggregate.order.exception.BizOrderPaymentMismatchException;
import com.hw.aggregate.order.exception.BizOrderUpdateAddressAfterPaymentException;
import com.hw.aggregate.order.model.product.AppProductOption;
import com.hw.aggregate.order.model.product.AppProductSku;
import com.hw.aggregate.order.model.product.AppProductSumPagedRep;
import com.hw.aggregate.order.representation.UserBizOrderRep;
import com.hw.shared.Auditable;
import com.hw.shared.UserThreadLocal;
import com.hw.shared.rest.IdBasedEntity;
import com.hw.shared.rest.VersionBasedEntity;
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
import java.io.Serializable;
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
public class BizOrder extends Auditable implements IdBasedEntity, VersionBasedEntity, Serializable {
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
    private Long userId;
    public static final String ENTITY_USER_ID = "userId";

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
    private boolean paid;

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

    public BizOrder(AppCreateBizOrderCommand command) {
        List<BizOrderItem> collect2 = command.getProductList();
        this.readOnlyProductList = new ArrayList<>(collect2);
        this.paymentAmt = command.getPaymentAmt();
        validatePaymentAmount();
        this.id = command.getOrderId();
        this.writeOnlyProductList = collect2;
        this.userId = command.getUserId();
        BizOrderAddress address = new BizOrderAddress();
        address.setOrderAddressCity(command.getAddress().getCity());
        address.setOrderAddressCountry(command.getAddress().getCountry());
        address.setOrderAddressFullName(command.getAddress().getFullName());
        address.setOrderAddressLine1(command.getAddress().getLine1());
        address.setOrderAddressLine2(command.getAddress().getLine2());
        address.setOrderAddressPhoneNumber(command.getAddress().getPhoneNumber());
        address.setOrderAddressProvince(command.getAddress().getProvince());
        address.setOrderAddressPostalCode(command.getAddress().getPostalCode());
        this.address = address;
        this.paymentType = command.getPaymentType();
        this.paymentLink = command.getPaymentLink();
        this.modifiedByUserAt = Date.from(Instant.now());
        this.orderState = command.getOrderState();
        this.paid = false;
    }

    public static void prepare(long id, UserCreateBizOrderCommand command, SagaOrchestratorService sagaOrchestratorService, String changeId) {
        log.debug("start of createNew");
        SagaOrchestratorService.CreateBizStateMachineCommand machineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
        List<BizOrderItem> collect2 = getBizOrderItems(command.getProductList());
        machineCommand.setOrderId(id);
        machineCommand.setBizOrderEvent(BizOrderEvent.NEW_ORDER);
        machineCommand.setCreatedBy(UserThreadLocal.get());
        machineCommand.setUserId(Long.parseLong(UserThreadLocal.get()));
        machineCommand.setOrderState(BizOrderStatus.DRAFT);
        machineCommand.setPrepareEvent(BizOrderEvent.PREPARE_NEW_ORDER);
        machineCommand.setOrderStorageChange(BizOrder.getReserveOrderPatchCommands(collect2));
        machineCommand.setTxId(changeId);
        machineCommand.setPaymentAmt(command.getPaymentAmt());
        machineCommand.setPaymentType(command.getPaymentType());
        machineCommand.setAddress(command.getAddress());
        machineCommand.setProductList(collect2);
        sagaOrchestratorService.startTx(List.of(machineCommand));
    }

    public static BizOrder create(AppCreateBizOrderCommand command) {
        return new BizOrder(command);
    }

    public void updateModifiedByUserAt() {
        this.modifiedByUserAt = Date.from(Instant.now());
    }

    private static List<BizOrderItem> getBizOrderItems(List<BizOrderItemCommand> productList) {
        return productList.stream().map(e -> {
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
    }

    public static List<PatchCommand> getReserveOrderPatchCommands(List<BizOrderItem> collect2) {
        List<PatchCommand> details = new ArrayList<>();
        collect2.forEach(e -> {
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

    private static String getPatchPath(BizOrderItem e, String fieldName) {
        String replace = String.join(",", e.getAttributesSales()).replace(":", "-").replace("/", "~/");
        return "/" + e.getProductId() + "/skus?query=attributesSales:" + replace + "/" + fieldName;
    }

    public static List<PatchCommand> getConfirmOrderPatchCommands(List<BizOrderItem> collect2) {
        List<PatchCommand> details = new ArrayList<>();
        collect2.forEach(e -> {
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

    public void updateAddress(UserUpdateBizOrderAddressCommand command) {
        if (this.orderState.equals(BizOrderStatus.PAID_RECYCLED) || this.orderState.equals(BizOrderStatus.PAID_RESERVED)) {
            throw new BizOrderUpdateAddressAfterPaymentException();
        }
        if (StringUtils.hasText(command.getCountry())
                && StringUtils.hasText(command.getProvince())
                && StringUtils.hasText(command.getCity())
                && StringUtils.hasText(command.getPostalCode())
                && StringUtils.hasText(command.getLine1())
                && StringUtils.hasText(command.getFullName())
                && StringUtils.hasText(command.getPhoneNumber())
        ) {
            address.setOrderAddressCountry(command.getCountry());
            address.setOrderAddressProvince(command.getProvince());
            address.setOrderAddressCity(command.getCity());
            address.setOrderAddressPostalCode(command.getPostalCode());
            address.setOrderAddressLine1(command.getLine1());
            address.setOrderAddressLine2(command.getLine2());
            address.setOrderAddressFullName(command.getFullName());
            address.setOrderAddressPhoneNumber(command.getPhoneNumber());
        }
        updateModifiedByUserAt();
    }

    public static void confirmPayment(SagaOrchestratorService sagaOrchestratorService, String changeId, UserBizOrderRep rep) {
        log.debug("start of confirmPayment");
        SagaOrchestratorService.CreateBizStateMachineCommand machineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
        machineCommand.setOrderId(rep.getId());
        machineCommand.setBizOrderEvent(BizOrderEvent.CONFIRM_PAYMENT);
        machineCommand.setPrepareEvent(BizOrderEvent.PREPARE_CONFIRM_PAYMENT);
        machineCommand.setActualStorageChange(BizOrder.getConfirmOrderPatchCommands(rep.getProductList()));
        machineCommand.setCreatedBy(UserThreadLocal.get());
        machineCommand.setUserId(Long.parseLong(UserThreadLocal.get()));
        machineCommand.setOrderState(rep.getOrderState());
        machineCommand.setTxId(changeId);
        machineCommand.setVersion(rep.getVersion());
        sagaOrchestratorService.startTx(List.of(machineCommand));
    }

    public static void reserve(SagaOrchestratorService sagaOrchestratorService, String changeId, UserBizOrderRep rep) {
        log.info("reserve order {} again", rep.getId());
        log.info("order status {}", rep.getOrderState());
        SagaOrchestratorService.CreateBizStateMachineCommand machineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
        machineCommand.setOrderId(rep.getId());
        machineCommand.setBizOrderEvent(BizOrderEvent.RESERVE);
        machineCommand.setPrepareEvent(BizOrderEvent.PREPARE_RESERVE);
        machineCommand.setCreatedBy(UserThreadLocal.get());
        machineCommand.setOrderStorageChange(BizOrder.getReserveOrderPatchCommands(rep.getProductList()));
        machineCommand.setUserId(Long.parseLong(UserThreadLocal.get()));
        machineCommand.setOrderState(rep.getOrderState());
        machineCommand.setTxId(changeId);
        machineCommand.setVersion(rep.getVersion());
        sagaOrchestratorService.startTx(List.of(machineCommand));
    }

    public static boolean validateProducts(AppProductSumPagedRep appProductSumPagedRep, List<BizOrderItem> orderItems) {
        return orderItems.stream().noneMatch(command -> {
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

    public BizOrder replace(AppUpdateBizOrderCommand command) {
        this.setOrderState(command.getOrderState());
        this.setPaid(command.getPaymentStatus() == null ? false : command.getPaymentStatus());
        this.setVersion(command.getVersion());
        return this;
    }

    public static BizOrder getWOptLockForUser(Long id, String userId, BizOrderRepository orderRepository) {
        Optional<BizOrder> byId = orderRepository.findByIdOptLockForUser(id, Long.parseLong(userId));
        if (byId.isEmpty())
            throw new EntityNotExistException();
        return byId.get();
    }
    public static BizOrder getWOptLockForApp(Long id, BizOrderRepository orderRepository) {
        Optional<BizOrder> byId = orderRepository.findByIdOptLockForApp(id);
        if (byId.isEmpty())
            throw new EntityNotExistException();
        return byId.get();
    }
}

