package org.activiti.runtime.api.model.payloads;

import java.util.Date;

public class UpdateTaskPayload {

    private String taskId;
    private String taskName;
    private String description;
    private Date dueDate;
    private int priority;
    private String assignee;

    public UpdateTaskPayload() {
    }

    public UpdateTaskPayload(String taskId,
                             String taskName,
                             String description,
                             Date dueDate,
                             int priority,
                             String assignee) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.assignee = assignee;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
