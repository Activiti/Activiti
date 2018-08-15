package org.activiti.runtime.api.model.payloads;

import java.util.Date;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class UpdateTaskPayload implements Payload {

    private String id;
    private String taskId;
    private String taskName;
    private String description;
    private Date dueDate;
    private Integer priority;
    private String assignee;

    public UpdateTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public UpdateTaskPayload(String taskId,
                             String taskName,
                             String description,
                             Date dueDate,
                             Integer priority,
                             String assignee) {
        this();
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
