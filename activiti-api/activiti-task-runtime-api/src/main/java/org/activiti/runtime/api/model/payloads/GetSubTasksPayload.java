package org.activiti.runtime.api.model.payloads;

import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class GetSubTasksPayload implements Payload {

    private String id;
    private String parentTaskId;

    public GetSubTasksPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetSubTasksPayload(String parentTaskId) {
        this();
        this.parentTaskId = parentTaskId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
}
