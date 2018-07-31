package org.activiti.runtime.api.model.payloads;

import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class GetTaskVariablesPayload implements Payload {

    private String id;
    private String taskId;
    private boolean localOnly;

    public GetTaskVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetTaskVariablesPayload(String taskId,
                                   boolean localOnly) {
        this();
        this.taskId = taskId;
        this.localOnly = localOnly;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }
}
