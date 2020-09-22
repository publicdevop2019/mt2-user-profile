package com.hw.aggregate.task.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import com.hw.shared.sql.clause.SelectFieldLongEqualClause;
import com.hw.shared.sql.clause.SelectFieldStringEqualClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static com.hw.aggregate.task.model.BizTask.*;
@Component
public class AdminBizTaskSelectQueryBuilder extends SelectQueryBuilder<BizTask> {
    AdminBizTaskSelectQueryBuilder() {
        supportedWhereField.put(ENTITY_TASK_NAME, new SelectFieldStringEqualClause<>(ENTITY_TASK_NAME));
        supportedWhereField.put(ENTITY_TASK_STATUS, new SelectFieldStringEqualClause<>(ENTITY_TASK_STATUS));
        supportedWhereField.put(ENTITY_REFERENCE_ID, new SelectFieldLongEqualClause<>(ENTITY_REFERENCE_ID));
        allowEmptyClause=true;
    }

    @Autowired
    private void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }
}
