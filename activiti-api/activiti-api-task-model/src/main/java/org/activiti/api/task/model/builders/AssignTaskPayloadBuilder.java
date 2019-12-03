package org.activiti.api.task.model.builders;

import org.activiti.api.task.model.payloads.AssignTaskPayload;

public class AssignTaskPayloadBuilder {

    private String taskId;
    private String assignee;

    public AssignTaskPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public AssignTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public AssignTaskPayload build() {
        return new AssignTaskPayload(taskId,
                                     assignee);
    }
}
