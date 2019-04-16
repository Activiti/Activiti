package org.activiti.api.task.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.task.model.payloads.SaveTaskPayload;

public class SaveTaskPayloadBuilder {

    private String taskId;
    private Map<String, Object> taskVariables;

    public SaveTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public SaveTaskPayloadBuilder withVariables(Map<String, Object> taskVariables) {
        this.taskVariables = taskVariables;
        return this;
    }

    public SaveTaskPayloadBuilder withTaskVariable(String name,
                                                   Object value) {
        if (this.taskVariables == null) {
            this.taskVariables = new HashMap<>();
        }
        this.taskVariables.put(name,
                           value);
        return this;
    }

    public SaveTaskPayload build() {
        return new SaveTaskPayload(taskId,
                                   taskVariables);
    }
}
