package org.activiti.runtime.api.model.builder;

import java.util.Map;

import org.activiti.runtime.api.model.payloads.SetTaskVariablesPayload;

public class SetTaskVariablesPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;
    private boolean localOnly;

    public SetTaskVariablesPayloadBuilder withLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public SetTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public SetTaskVariablesPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public SetTaskVariablesPayload build() {
        return new SetTaskVariablesPayload(taskId,
                                           variables,
                                           localOnly);
    }
}
