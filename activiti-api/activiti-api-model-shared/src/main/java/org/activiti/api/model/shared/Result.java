package org.activiti.api.model.shared;

import java.io.Serializable;

public abstract class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Payload payload;
    private T entity;

    public Result() {
    }

    public Result(Payload payload,
                  T entity) {
        this.payload = payload;
        this.entity = entity;
    }

    public Payload getPayload() {
        return payload;
    }

    public T getEntity() {
        return entity;
    }
}
