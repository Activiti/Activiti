package org.activiti.api.task.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;



public class AssignTaskPayload implements Payload {

    private String id;
    private String taskId;
    private String assignee;

    public AssignTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public AssignTaskPayload(String taskId,
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
