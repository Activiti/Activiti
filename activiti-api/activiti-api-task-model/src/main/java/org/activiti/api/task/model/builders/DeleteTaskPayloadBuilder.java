package org.activiti.api.task.model.builders;


import org.activiti.api.task.model.payloads.DeleteTaskPayload;

public class DeleteTaskPayloadBuilder {

    private String taskId;
    private String reason;

    public DeleteTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public DeleteTaskPayloadBuilder withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public DeleteTaskPayload build() {
        return new DeleteTaskPayload(taskId,
                                     reason);
    }
}
