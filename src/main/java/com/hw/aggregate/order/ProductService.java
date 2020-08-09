package com.hw.aggregate.order;

import com.hw.aggregate.order.exception.ActualStorageDecreaseException;
import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.StorageChange;
import com.hw.aggregate.order.model.StorageChangeDetail;
import com.hw.aggregate.order.model.product.AppProductSumPagedRep;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.hw.shared.AppConstant.HTTP_PARAM_QUERY;

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

    @Value("${url.productUrl}")
    private String productUrl;

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

    public AppProductSumPagedRep getProductsInfo(List<BizOrderItem> customerOrderItemList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<BizOrderItem>> hashMapHttpEntity = new HttpEntity<>(customerOrderItemList, headers);

        List<String> collect = customerOrderItemList.stream().map(e -> e.getProductId().toString()).collect(Collectors.toList());
        String query = getQuery(collect);
        ResponseEntity<AppProductSumPagedRep> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + productUrl + query, HttpMethod.GET, hashMapHttpEntity, AppProductSumPagedRep.class);
        if (exchange.getStatusCode() != HttpStatus.OK)
            throw new ProductInfoValidationException();
        return exchange.getBody();
    }

    private String getQuery(List<String> ids) {
        return "?" + HTTP_PARAM_QUERY + "=id:" + String.join(".", ids);

    }

}
