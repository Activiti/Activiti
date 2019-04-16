package org.activiti.api.task.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.task.model.payloads.CompleteTaskPayload;

public class CompleteTaskPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;
    private Map<String, Object> taskVariables;

    public CompleteTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public CompleteTaskPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public CompleteTaskPayloadBuilder withTaskVariables(Map<String, Object> taskVariables) {
        this.taskVariables = taskVariables;
        return this;
    }
    
    public CompleteTaskPayloadBuilder withVariable(String name,
                                                   Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public CompleteTaskPayloadBuilder withTaskVariable(String name,
                                                       Object value) {
        if (this.taskVariables == null) {
            this.taskVariables = new HashMap<>();
        }
        this.taskVariables.put(name,
                           value);
        return this;
    }
    
    public CompleteTaskPayload build() {
        return new CompleteTaskPayload(taskId,
                                       variables,
                                       taskVariables);
    }
}
