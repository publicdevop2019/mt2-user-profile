package com.hw.aggregate.order.model;

import com.hw.shared.sql.clause.WhereClause;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.hw.aggregate.order.model.BizOrder.ENTITY_ORDER_ORDER_STATE;

@Component
public class NotPayedClause extends WhereClause<BizOrder> {
    @Override
    public Predicate getWhereClause(String query, CriteriaBuilder cb, Root<BizOrder> root, AbstractQuery<?> abstractQuery) {
        Predicate equal = cb.equal(root.get(ENTITY_ORDER_ORDER_STATE).as(String.class), BizOrderStatus.NOT_PAID_RECYCLED);
        Predicate equal2 = cb.equal(root.get(ENTITY_ORDER_ORDER_STATE).as(String.class), BizOrderStatus.NOT_PAID_RESERVED);
        return cb.or(equal, equal2);
    }
}
