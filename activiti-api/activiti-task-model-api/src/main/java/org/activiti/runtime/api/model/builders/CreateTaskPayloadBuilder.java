package org.activiti.runtime.api.model.builders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.runtime.api.model.payloads.CreateTaskPayload;

public class CreateTaskPayloadBuilder {

    private String name;
    private String description;
    private Date dueDate;
    private int priority;
    private String assignee;
    private List<String> groups;
    private String parentTaskId;

    public CreateTaskPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreateTaskPayloadBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CreateTaskPayloadBuilder withDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public CreateTaskPayloadBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public CreateTaskPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public CreateTaskPayloadBuilder withParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
        return this;
    }

    public CreateTaskPayloadBuilder withGroups(List<String> groups) {
        this.groups = groups;
        return this;
    }

    public CreateTaskPayloadBuilder withGroup(String group) {
        if (this.groups == null) {
            this.groups = new ArrayList<>();
        }
        this.groups.add(group);
        return this;
    }

    public CreateTaskPayload build() {
        return new CreateTaskPayload(name,
                                     description,
                                     dueDate,
                                     priority,
                                     assignee,
                                     groups,
                                     parentTaskId);
    }
}
