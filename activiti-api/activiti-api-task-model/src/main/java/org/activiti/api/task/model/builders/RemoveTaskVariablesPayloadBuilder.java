package org.activiti.api.task.model.builders;

import java.util.Date;

import org.activiti.api.task.model.payloads.UpdateTaskPayload;

public class RemoveTaskVariablesPayloadBuilder {

    private String taskId;
    private String name;
    private String description;
    private Date dueDate;
    private int priority;
    private String assignee;

    public RemoveTaskVariablesPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public UpdateTaskPayload build() {
        return new UpdateTaskPayload();
    }
}
