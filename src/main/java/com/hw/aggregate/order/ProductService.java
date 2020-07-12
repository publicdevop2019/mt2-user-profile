package com.hw.aggregate.order;

import com.hw.aggregate.order.exception.ActualStorageDecreaseException;
import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.StorageChange;
import com.hw.aggregate.order.model.StorageChangeDetail;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Value("${url.decreaseUrl}")
    private String decreaseUrl;

    @Value("${url.sold}")
    private String soldUrl;

    @Value("${url.increaseUrl}")
    private String increaseUrl;

    @Value("${url.validateUrl}")
    private String validateUrl;

    @Value("${url.revoke}")
    private String rollbackUrl;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;


    public void decreaseOrderStorage(List<StorageChangeDetail> changeList, String txId) {
        StorageChange storageChange = new StorageChange();
        storageChange.setTxId(txId);
        storageChange.setChangeList(changeList);
        changeStorage(decreaseUrl, storageChange);
    }

    public void increaseOrderStorage(List<StorageChangeDetail> changeList, String txId) {
        StorageChange storageChange = new StorageChange();
        storageChange.setTxId(txId);
        storageChange.setChangeList(changeList);
        changeStorage(increaseUrl, storageChange);
    }

    public void rollbackTransaction(String txId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + rollbackUrl + "?txId=" + txId, HttpMethod.PUT, hashMapHttpEntity, String.class);
    }

    public void decreaseActualStorage(List<StorageChangeDetail> changeList, String txId) throws ActualStorageDecreaseException {
        StorageChange storageChange = new StorageChange();
        storageChange.setTxId(txId);
        storageChange.setChangeList(changeList);
        changeStorage(soldUrl, storageChange);
    }

    private void changeStorage(String url, StorageChange change) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StorageChange> hashMapHttpEntity = new HttpEntity<>(change, headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + url, HttpMethod.PUT, hashMapHttpEntity, String.class);
    }

    public void validateProductInfo(List<BizOrderItem> customerOrderItemList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<BizOrderItem>> hashMapHttpEntity = new HttpEntity<>(customerOrderItemList, headers);
        ParameterizedTypeReference<HashMap<String, String>> responseType =
                new ParameterizedTypeReference<>() {
                };
        ResponseEntity<HashMap<String, String>> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + validateUrl, HttpMethod.POST, hashMapHttpEntity, responseType);
        if (exchange.getBody() == null || !"true".equals(exchange.getBody().get("result")))
            throw new ProductInfoValidationException();
    }

}
