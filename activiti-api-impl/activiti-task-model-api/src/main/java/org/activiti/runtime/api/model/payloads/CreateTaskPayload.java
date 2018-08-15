package org.activiti.runtime.api.model.payloads;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class CreateTaskPayload implements Payload {

    private String id;
    private String name;
    private String description;
    private Date dueDate;
    private int priority;
    private String assignee;
    private List<String> groups;
    private String parentTaskId;

    public CreateTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public CreateTaskPayload(String name,
                             String description,
                             Date dueDate,
                             int priority,
                             String assignee,
                             List<String> groups,
                             String parentTaskId) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.assignee = assignee;
        this.groups = groups;
        this.parentTaskId = parentTaskId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
