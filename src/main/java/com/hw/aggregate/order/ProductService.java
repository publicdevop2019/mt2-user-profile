package com.hw.aggregate.order;

import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.aggregate.order.model.BizOrderItem;
import com.hw.aggregate.order.model.product.AppProductSumPagedRep;
import com.hw.shared.EurekaRegistryHelper;
import com.hw.shared.PatchCommand;
import com.hw.shared.ResourceServiceTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.hw.shared.AppConstant.HTTP_HEADER_CHANGE_ID;
import static com.hw.shared.AppConstant.HTTP_PARAM_QUERY;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private EurekaRegistryHelper eurekaRegistryHelper;

    @Value("${url.products.app}")
    private String productUrl;

    @Value("${url.products.change.app}")
    private String change;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;


    public void updateProductStorage(List<PatchCommand> changeList, String txId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HTTP_HEADER_CHANGE_ID, txId);
        HttpEntity<List<PatchCommand>> hashMapHttpEntity = new HttpEntity<>(changeList, headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + productUrl, HttpMethod.PATCH, hashMapHttpEntity, String.class);
    }

    public void rollbackTransaction(String changeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> hashMapHttpEntity = new HttpEntity<>(headers);
        tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + change + "/" + changeId, HttpMethod.DELETE, hashMapHttpEntity, String.class);
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
