package com.hw.aggregate.order;

import com.hw.aggregate.order.model.BizOrderAddressCmdRep;
import com.hw.aggregate.order.model.BizOrderEvent;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.BizOrderStatus;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import com.hw.shared.sql.PatchCommand;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SagaOrchestratorService {
    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Value("${url.saga.app}")
    private String sageUrl;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    public void startTx(List<CreateBizStateMachineCommand> createBizStateMachineCommand) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<CreateBizStateMachineCommand>> hashMapHttpEntity = new HttpEntity<>(createBizStateMachineCommand, headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + sageUrl, HttpMethod.POST, hashMapHttpEntity, Void.class);
    }

    @Data
    public static class CreateBizStateMachineCommand {
        private long orderId;
        private long userId;
        private String txId;
        private BizOrderStatus orderState;
        private BizOrderEvent bizOrderEvent;
        private BizOrderEvent prepareEvent;
        private List<BizOrderItem> productList;
        private String createdBy;
        private List<PatchCommand> orderStorageChange;
        private List<PatchCommand> actualStorageChange;
        private BizOrderAddressCmdRep address;
        private String paymentType;
        private BigDecimal paymentAmt;
        private Integer version;
    }
}
