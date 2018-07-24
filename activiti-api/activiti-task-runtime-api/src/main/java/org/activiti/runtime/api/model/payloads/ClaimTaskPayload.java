package org.activiti.runtime.api.model.payloads;

public class ClaimTaskPayload {

    private String taskId;
    private String assignee;

    public ClaimTaskPayload() {
    }

    public ClaimTaskPayload(String taskId,
                            String assignee) {
        this.taskId = taskId;
        this.assignee = assignee;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
