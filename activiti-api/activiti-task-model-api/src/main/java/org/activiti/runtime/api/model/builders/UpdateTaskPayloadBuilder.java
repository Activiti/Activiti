package org.activiti.runtime.api.model.builders;

import java.util.Date;

import org.activiti.runtime.api.model.payloads.UpdateTaskPayload;

public class UpdateTaskPayloadBuilder {

    private String taskId;
    private String name;
    private String description;
    private Date dueDate;
    private Integer priority;
    private String assignee;

    public UpdateTaskPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public UpdateTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public UpdateTaskPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateTaskPayloadBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public UpdateTaskPayloadBuilder withDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public UpdateTaskPayloadBuilder withPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public UpdateTaskPayload build() {
        return new UpdateTaskPayload(taskId,
                                     name,
                                     description,
                                     dueDate,
                                     priority,
                                     assignee);
    }
}
