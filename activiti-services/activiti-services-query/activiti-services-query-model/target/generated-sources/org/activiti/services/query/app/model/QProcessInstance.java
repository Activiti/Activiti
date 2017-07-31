package org.activiti.services.query.app.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProcessInstance is a Querydsl query type for ProcessInstance
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProcessInstance extends EntityPathBase<ProcessInstance> {

    private static final long serialVersionUID = -1955019391L;

    public static final QProcessInstance processInstance = new QProcessInstance("processInstance");

    public final DateTimePath<java.util.Date> lastModified = createDateTime("lastModified", java.util.Date.class);

    public final DateTimePath<java.util.Date> lastModifiedFrom = createDateTime("lastModifiedFrom", java.util.Date.class);

    public final DateTimePath<java.util.Date> lastModifiedTo = createDateTime("lastModifiedTo", java.util.Date.class);

    public final StringPath processDefinitionId = createString("processDefinitionId");

    public final NumberPath<Long> processInstanceId = createNumber("processInstanceId", Long.class);

    public final StringPath status = createString("status");

    public final ListPath<Variable, QVariable> variables = this.<Variable, QVariable>createList("variables", Variable.class, QVariable.class, PathInits.DIRECT2);

    public QProcessInstance(String variable) {
        super(ProcessInstance.class, forVariable(variable));
    }

    public QProcessInstance(Path<? extends ProcessInstance> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProcessInstance(PathMetadata metadata) {
        super(ProcessInstance.class, metadata);
    }

}

