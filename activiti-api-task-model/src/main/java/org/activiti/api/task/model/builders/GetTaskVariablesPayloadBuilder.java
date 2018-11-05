package org.activiti.api.task.model.builders;

import org.activiti.api.task.model.payloads.GetTaskVariablesPayload;

import java.util.Collection;

public class GetTaskVariablesPayloadBuilder {

    private String taskId;
    private Collection<String> variableNames;

    public GetTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public GetTaskVariablesPayloadBuilder withVariableNames(Collection<String> variableNames) {
        this.variableNames = variableNames;
        return this;
    }

    public GetTaskVariablesPayload build() {
        return new GetTaskVariablesPayload(taskId, variableNames);
    }
}
