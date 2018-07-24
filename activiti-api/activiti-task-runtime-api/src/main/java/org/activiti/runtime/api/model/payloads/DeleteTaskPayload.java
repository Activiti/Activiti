package org.activiti.runtime.api.model.payloads;

public class DeleteTaskPayload {

    private String taskId;
    private String reason;

    public DeleteTaskPayload() {
    }

    public DeleteTaskPayload(String taskId,
                             String reason) {
        this.taskId = taskId;
        this.reason = reason;
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

    public void setReason(String reason) {
        this.reason = reason;
    }
}
