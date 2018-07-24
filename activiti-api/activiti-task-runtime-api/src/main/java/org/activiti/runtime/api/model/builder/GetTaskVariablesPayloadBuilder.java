package org.activiti.runtime.api.model.builder;

import org.activiti.runtime.api.model.payloads.GetTaskVariablesPayload;

public class GetTaskVariablesPayloadBuilder {

    private String taskId;
    private boolean localOnly;

    public GetTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public GetTaskVariablesPayloadBuilder withLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public GetTaskVariablesPayload build() {
        return new GetTaskVariablesPayload(taskId, localOnly);
    }
}
