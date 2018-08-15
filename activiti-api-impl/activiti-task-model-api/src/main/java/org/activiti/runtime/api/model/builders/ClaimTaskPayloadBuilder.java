package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.payloads.ClaimTaskPayload;

public class ClaimTaskPayloadBuilder {

    private String taskId;
    private String assignee;

    public ClaimTaskPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public ClaimTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public ClaimTaskPayload build() {
        return new ClaimTaskPayload(taskId,
                                    assignee);
    }
}
