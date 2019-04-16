package org.activiti.api.task.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.task.model.payloads.SaveTaskVariablesPayload;

public class SaveTaskVariablesPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;

    public SaveTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public SaveTaskVariablesPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public SaveTaskVariablesPayloadBuilder withVariable(String name,
                                                        Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public SaveTaskVariablesPayload build() {
        return new SaveTaskVariablesPayload(taskId,
                                            variables);
    }
}
