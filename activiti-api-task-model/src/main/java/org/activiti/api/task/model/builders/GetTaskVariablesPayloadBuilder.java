package org.activiti.api.task.model.builders;

import org.activiti.api.task.model.payloads.GetTaskVariablesPayload;

import java.util.Collection;

public class GetTaskVariablesPayloadBuilder {

    private String taskId;

    public GetTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public GetTaskVariablesPayload build() {
        return new GetTaskVariablesPayload(taskId);
    }
}
