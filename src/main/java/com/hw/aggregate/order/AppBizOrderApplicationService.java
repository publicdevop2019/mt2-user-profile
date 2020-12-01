package com.hw.aggregate.order;

import com.hw.aggregate.order.command.AppCreateBizOrderCommand;
import com.hw.aggregate.order.command.AppUpdateBizOrderCommand;
import com.hw.aggregate.order.command.AppValidateBizOrderCommand;
import com.hw.aggregate.order.exception.ProductInfoValidationException;
import com.hw.aggregate.order.model.BizOrder;
import com.hw.aggregate.order.model.product.AppProductSumPagedRep;
import com.hw.aggregate.order.representation.AppBizOrderRep;
import com.hw.shared.rest.RoleBasedRestfulService;
import com.hw.shared.rest.VoidTypedClass;
import com.hw.shared.sql.RestfulQueryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppBizOrderApplicationService extends RoleBasedRestfulService<BizOrder, Void, AppBizOrderRep, VoidTypedClass> {
    {
        entityClass = BizOrder.class;
        role = RestfulQueryRegistry.RoleEnum.APP;
    }

    @Autowired
    private ProductService productService;

    @Override
    protected Long generateId(Object object) {
        return ((AppCreateBizOrderCommand) object).getOrderId();
    }

    public void validate(AppValidateBizOrderCommand command) {
        AppProductSumPagedRep productsInfo = productService.getProductsInfo(command.getProductList());
        if (!BizOrder.validateProducts(productsInfo, command.getProductList())) {
            throw new ProductInfoValidationException();
        }
    }

    @Override
    public BizOrder replaceEntity(BizOrder bizOrder, Object command) {
        return bizOrder.replace((AppUpdateBizOrderCommand) command);
    }

    @Override
    public AppBizOrderRep getEntityRepresentation(BizOrder bizOrder) {
        return new AppBizOrderRep(bizOrder);
    }

    @Override
    protected BizOrder createEntity(long id, Object command) {
        return BizOrder.create((AppCreateBizOrderCommand) command);
    }
}
