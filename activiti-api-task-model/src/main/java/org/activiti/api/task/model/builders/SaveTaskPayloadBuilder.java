package org.activiti.api.task.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.task.model.payloads.SaveTaskPayload;

public class SaveTaskPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;

    public SaveTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public SaveTaskPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public SaveTaskPayloadBuilder withVariable(String name,
                                                        Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public SaveTaskPayload build() {
        return new SaveTaskPayload(taskId,
                                            variables);
    }
}
