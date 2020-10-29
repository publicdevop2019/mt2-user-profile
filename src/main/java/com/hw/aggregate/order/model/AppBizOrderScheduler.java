package com.hw.aggregate.order.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hw.aggregate.order.BizOrderRepository;
import com.hw.aggregate.order.ProductService;
import com.hw.aggregate.order.SagaOrchestratorService;
import com.hw.shared.sql.PatchCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.hw.shared.AppConstant.PATCH_OP_TYPE_DIFF;
import static com.hw.shared.AppConstant.PATCH_OP_TYPE_SUM;

@Service
@Slf4j
@EnableScheduling
public class AppBizOrderScheduler {

    @Value("${order.expireAfter}")
    private Long expireAfter;

    @Autowired
    private BizOrderRepository bizOrderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SagaOrchestratorService sagaOrchestratorService;

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.release}")
    public void releaseExpiredOrder() {
        Date from = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - expireAfter * 60 * 1000));
        List<BizOrder> expiredOrderList = bizOrderRepository.findExpiredNotPaidReserved(from);
        if (!expiredOrderList.isEmpty()) {
            List<SagaOrchestratorService.CreateBizStateMachineCommand> releaseCmds = new ArrayList<>();
            expiredOrderList.forEach(expiredOrder -> {
                List<PatchCommand> var1 = BizOrder.getReserveOrderPatchCommands(expiredOrder.getReadOnlyProductList());
                // convert to release order storage change
                var1.forEach(e -> {
                    if (e.getOp().equalsIgnoreCase(PATCH_OP_TYPE_SUM)) {
                        e.setOp(PATCH_OP_TYPE_DIFF);
                    } else {
                        e.setOp(PATCH_OP_TYPE_SUM);
                    }
                });
                SagaOrchestratorService.CreateBizStateMachineCommand createBizStateMachineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
                createBizStateMachineCommand.setTxId(UUID.randomUUID().toString());
                createBizStateMachineCommand.setOrderId(expiredOrder.getId());
                createBizStateMachineCommand.setOrderStorageChange(var1);
                createBizStateMachineCommand.setOrderState(BizOrderStatus.NOT_PAID_RESERVED);
                createBizStateMachineCommand.setPrepareEvent(BizOrderEvent.PREPARE_RECYCLE_ORDER_STORAGE);
                createBizStateMachineCommand.setBizOrderEvent(BizOrderEvent.RECYCLE_ORDER_STORAGE);
                createBizStateMachineCommand.setVersion(expiredOrder.getVersion());

                releaseCmds.add(createBizStateMachineCommand);
            });
            log.info("expired order(s) found {}", expiredOrderList.stream().map(BizOrder::getId).collect(Collectors.toList()).toString());
            sagaOrchestratorService.startTx(releaseCmds);
        }
    }

    /**
     * resubmit order in paid_reserved and paid_recycled
     */
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.resubmit}")
    public void resubmitOrder() {
        List<BizOrder> paidReserved = bizOrderRepository.findPaidReserved();
        List<SagaOrchestratorService.CreateBizStateMachineCommand> confirmCmd = new ArrayList<>();
        if (!paidReserved.isEmpty()) {
            log.info("paid reserved order(s) found {}", paidReserved.stream().map(BizOrder::getId).collect(Collectors.toList()));
            paidReserved.forEach(order -> {
                SagaOrchestratorService.CreateBizStateMachineCommand createBizStateMachineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
                createBizStateMachineCommand.setTxId(UUID.randomUUID().toString());
                createBizStateMachineCommand.setOrderId(order.getId());
                createBizStateMachineCommand.setOrderStorageChange(BizOrder.getReserveOrderPatchCommands(order.getReadOnlyProductList()));
                createBizStateMachineCommand.setActualStorageChange(BizOrder.getConfirmOrderPatchCommands(order.getReadOnlyProductList()));
                createBizStateMachineCommand.setOrderState(order.getOrderState());
                createBizStateMachineCommand.setPrepareEvent(BizOrderEvent.PREPARE_CONFIRM_ORDER);
                createBizStateMachineCommand.setBizOrderEvent(BizOrderEvent.CONFIRM_ORDER);
                createBizStateMachineCommand.setVersion(order.getVersion());
                confirmCmd.add(createBizStateMachineCommand);
            });
        }
        List<BizOrder> paidRecycled = bizOrderRepository.findPaidRecycled();
        if (!paidRecycled.isEmpty()) {
            log.info("paid recycled order(s) found {}", paidRecycled.stream().map(BizOrder::getId).collect(Collectors.toList()));
            paidRecycled.forEach(order -> {
                SagaOrchestratorService.CreateBizStateMachineCommand createBizStateMachineCommand = new SagaOrchestratorService.CreateBizStateMachineCommand();
                createBizStateMachineCommand.setTxId(UUID.randomUUID().toString());
                createBizStateMachineCommand.setOrderId(order.getId());
                createBizStateMachineCommand.setOrderStorageChange(BizOrder.getReserveOrderPatchCommands(order.getReadOnlyProductList()));
                createBizStateMachineCommand.setActualStorageChange(BizOrder.getConfirmOrderPatchCommands(order.getReadOnlyProductList()));
                createBizStateMachineCommand.setOrderState(order.getOrderState());
                createBizStateMachineCommand.setPrepareEvent(BizOrderEvent.PREPARE_RESERVE);
                createBizStateMachineCommand.setBizOrderEvent(BizOrderEvent.RESERVE);
                createBizStateMachineCommand.setVersion(order.getVersion());
                confirmCmd.add(createBizStateMachineCommand);
            });
        }
        if (!confirmCmd.isEmpty()) {
            sagaOrchestratorService.startTx(confirmCmd);
        }
    }

}
