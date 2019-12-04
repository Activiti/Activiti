package org.activiti.api.task.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class DeleteTaskPayload implements Payload {

    private String id;
    private String taskId;
    private String reason;

    public DeleteTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public DeleteTaskPayload(String taskId,
                             String reason) {
        this();
        this.taskId = taskId;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public boolean hasReason() {
        return reason != null;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
