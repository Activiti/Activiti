package org.activiti.api.task.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;



public class ClaimTaskPayload implements Payload {

    private String id;
    private String taskId;
    private String assignee;

    public ClaimTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public ClaimTaskPayload(String taskId,
                            String assignee) {
        this();
        this.taskId = taskId;
        this.assignee = assignee;
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

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
