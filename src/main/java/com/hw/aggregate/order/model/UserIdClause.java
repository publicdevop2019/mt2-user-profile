package com.hw.aggregate.order.model;

import com.hw.shared.UserIdNotFoundException;
import com.hw.shared.UserThreadLocal;
import com.hw.shared.sql.clause.WhereClause;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.hw.aggregate.order.model.BizOrder.ENTITY_USER_ID;

public class UserIdClause extends WhereClause<BizOrder> {
    @Override
    public Predicate getWhereClause(String query, CriteriaBuilder cb, Root<BizOrder> root, AbstractQuery<?> abstractQuery) {
        if (null == UserThreadLocal.get())
            throw new UserIdNotFoundException();
        return cb.equal(root.get(ENTITY_USER_ID), Long.parseLong(UserThreadLocal.get()));
    }
}
