package org.activiti.runtime.api.model.payloads;

import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class ReleaseTaskPayload implements Payload {

    private String id;
    private String taskId;

    public ReleaseTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    public ReleaseTaskPayload(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
