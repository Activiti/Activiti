package org.activiti.runtime.api;

public abstract class Result<T> {

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
