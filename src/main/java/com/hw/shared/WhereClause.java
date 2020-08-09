package com.hw.shared;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface WhereClause<T> {
    Predicate getWhereClause(Root<T> root, String search);
}
