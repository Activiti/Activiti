package org.activiti.runtime.api.model.builders;

import java.util.Map;

import org.activiti.runtime.api.model.payloads.SetTaskVariablesPayload;

public class SetTaskVariablesPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;
    private boolean localOnly = false;

    public SetTaskVariablesPayloadBuilder localOnly() {
        this.localOnly = true;
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
