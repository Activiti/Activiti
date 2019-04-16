package org.activiti.api.task.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.task.model.payloads.UpdateTaskVariablesPayload;

public class UpdateTaskVariablesPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;

    public UpdateTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public UpdateTaskVariablesPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public UpdateTaskVariablesPayloadBuilder withVariable(String name,
                                                        Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public UpdateTaskVariablesPayload build() {
        return new UpdateTaskVariablesPayload(taskId,
                                            variables);
    }
}
