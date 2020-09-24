package com.hw.aggregate.order;

import com.hw.aggregate.order.model.BizOrderItem;
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

    @Value("${url.products.app}")
    private String productUrl;

    @Autowired
    private ResourceServiceTokenHelper tokenHelper;

    public AppProductSumPagedRep getProductsInfo(List<BizOrderItem> customerOrderItemList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<BizOrderItem>> hashMapHttpEntity = new HttpEntity<>(customerOrderItemList, headers);

        List<String> collect = customerOrderItemList.stream().map(e -> e.getProductId().toString()).collect(Collectors.toList());
        String query = getQuery(collect);
        ResponseEntity<AppProductSumPagedRep> exchange = tokenHelper.exchange(eurekaRegistryHelper.getProxyHomePageUrl() + productUrl + query, HttpMethod.GET, hashMapHttpEntity, AppProductSumPagedRep.class);
        return exchange.getBody();
    }

    private String getQuery(List<String> ids) {
        return "?" + HTTP_PARAM_QUERY + "=id:" + String.join(".", ids);

    }

}
