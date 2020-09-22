package com.hw.aggregate.order.model;

import com.hw.aggregate.address.model.UserThreadLocal;
import com.hw.config.UserIdNotFoundException;
import com.hw.shared.sql.clause.WhereClause;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.hw.shared.Auditable.ENTITY_CREATED_BY;

public class CreatedByClause extends WhereClause<BizOrder> {
    @Override
    public Predicate getWhereClause(String query, CriteriaBuilder cb, Root<BizOrder> root) {
        if (null == UserThreadLocal.get())
            throw new UserIdNotFoundException();
        return cb.equal(root.get(ENTITY_CREATED_BY).as(String.class), UserThreadLocal.get());
    }
}
