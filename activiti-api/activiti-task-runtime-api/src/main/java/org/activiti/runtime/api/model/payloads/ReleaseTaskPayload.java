package org.activiti.runtime.api.model.payloads;

public class ReleaseTaskPayload {

    private String taskId;

    public ReleaseTaskPayload() {
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
