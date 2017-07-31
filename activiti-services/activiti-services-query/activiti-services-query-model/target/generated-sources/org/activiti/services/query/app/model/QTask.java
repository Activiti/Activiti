package org.activiti.services.query.app.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTask is a Querydsl query type for Task
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTask extends EntityPathBase<Task> {

    private static final long serialVersionUID = 513564744L;

    public static final QTask task = new QTask("task");

    public final StringPath assignee = createString("assignee");

    public final StringPath category = createString("category");

    public final DateTimePath<java.util.Date> createTime = createDateTime("createTime", java.util.Date.class);

    public final StringPath description = createString("description");

    public final DateTimePath<java.util.Date> dueDate = createDateTime("dueDate", java.util.Date.class);

    public final StringPath id = createString("id");

    public final DateTimePath<java.util.Date> lastModified = createDateTime("lastModified", java.util.Date.class);

    public final DateTimePath<java.util.Date> lastModifiedFrom = createDateTime("lastModifiedFrom", java.util.Date.class);

    public final DateTimePath<java.util.Date> lastModifiedTo = createDateTime("lastModifiedTo", java.util.Date.class);

    public final StringPath name = createString("name");

    public final StringPath nameLike = createString("nameLike");

    public final StringPath priority = createString("priority");

    public final StringPath processDefinitionId = createString("processDefinitionId");

    public final StringPath processInstanceId = createString("processInstanceId");

    public final StringPath status = createString("status");

    public final ListPath<Variable, QVariable> variables = this.<Variable, QVariable>createList("variables", Variable.class, QVariable.class, PathInits.DIRECT2);

    public QTask(String variable) {
        super(Task.class, forVariable(variable));
    }

    public QTask(Path<? extends Task> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTask(PathMetadata metadata) {
        super(Task.class, metadata);
    }

}

