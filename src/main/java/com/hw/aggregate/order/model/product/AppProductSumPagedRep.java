package com.hw.aggregate.order.model.product;

import lombok.Data;

import java.util.List;

@Data
public class AppProductSumPagedRep {
    protected List<ProductAdminCardRepresentation> data;
    protected Long totalItemCount;

    @Data
    public static class ProductAdminCardRepresentation {
        private Long id;
        private List<AppProductOption> selectedOptions;
        private List<AppProductSku> productSkuList;
    }
}
