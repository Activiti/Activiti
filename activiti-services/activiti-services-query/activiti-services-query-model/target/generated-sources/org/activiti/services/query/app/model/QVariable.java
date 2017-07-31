package org.activiti.services.query.app.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QVariable is a Querydsl query type for Variable
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QVariable extends EntityPathBase<Variable> {

    private static final long serialVersionUID = -1954421825L;

    public static final QVariable variable = new QVariable("variable");

    public final DateTimePath<java.util.Date> createTime = createDateTime("createTime", java.util.Date.class);

    public final StringPath executionId = createString("executionId");

    public final StringPath id = createString("id");

    public final DateTimePath<java.util.Date> lastUpdatedTime = createDateTime("lastUpdatedTime", java.util.Date.class);

    public final StringPath name = createString("name");

    public final StringPath processInstanceId = createString("processInstanceId");

    public final StringPath taskId = createString("taskId");

    public final StringPath type = createString("type");

    public QVariable(String variable) {
        super(Variable.class, forVariable(variable));
    }

    public QVariable(Path<? extends Variable> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVariable(PathMetadata metadata) {
        super(Variable.class, metadata);
    }

}

