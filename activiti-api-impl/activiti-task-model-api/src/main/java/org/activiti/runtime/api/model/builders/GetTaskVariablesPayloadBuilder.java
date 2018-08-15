package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.payloads.GetTaskVariablesPayload;

public class GetTaskVariablesPayloadBuilder {

    private String taskId;
    private boolean localOnly = false;

    public GetTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public GetTaskVariablesPayloadBuilder localOnly() {
        this.localOnly = true;
        return this;
    }

    public GetTaskVariablesPayload build() {
        return new GetTaskVariablesPayload(taskId, localOnly);
    }
}
