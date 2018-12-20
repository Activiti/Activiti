package org.activiti.api.task.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

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
        this();
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
