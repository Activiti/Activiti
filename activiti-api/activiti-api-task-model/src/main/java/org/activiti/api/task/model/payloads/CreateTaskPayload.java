package org.activiti.api.task.model.payloads;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class CreateTaskPayload implements Payload {

    private String id;
    private String name;
    private String description;
    private Date dueDate;
    private int priority;
    private String assignee;
    private List<String> candidateGroups;
    private List<String> candidateUsers;
    private String parentTaskId;
    private String formKey;

    public CreateTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public CreateTaskPayload(String name,
                             String description,
                             Date dueDate,
                             int priority,
                             String assignee,
                             List<String> candidateGroups,
                             List<String> candidateUsers,
                             String parentTaskId,
                             String formKey) {
        this();
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.assignee = assignee;
        this.candidateGroups = candidateGroups;
        this.candidateUsers = candidateUsers;
        this.parentTaskId = parentTaskId;
        this.formKey = formKey;
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

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public List<String> getCandidateGroups() {
        return candidateGroups;
    }

    public void setCandidateGroups(List<String> candidateGroups) {
        this.candidateGroups = candidateGroups;
    }

    public List<String> getCandidateUsers() {
        return candidateUsers;
    }

    public void setCandidateUsers(List<String> candidateUsers) {
        this.candidateUsers = candidateUsers;
    }
}
