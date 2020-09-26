package com.hw.aggregate.cart.model;

import com.hw.shared.UserThreadLocal;
import com.hw.shared.UserIdNotFoundException;
import com.hw.shared.sql.clause.WhereClause;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.hw.shared.Auditable.ENTITY_CREATED_BY;

@Slf4j
public class CreatedByClause extends WhereClause<BizCartItem> {
    @Override
    public Predicate getWhereClause(String query, CriteriaBuilder cb, Root<BizCartItem> root) {
        if (null == UserThreadLocal.get())
            throw new UserIdNotFoundException();
        return cb.equal(root.get(ENTITY_CREATED_BY).as(String.class), UserThreadLocal.get());
    }
}
