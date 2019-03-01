package org.activiti.api.task.model.builders;

import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;

public class UpdateTaskVariablePayloadBuilder {

    private String taskId;
    private String name;
    private Object value;

    public UpdateTaskVariablePayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public UpdateTaskVariablePayloadBuilder withVariable(String name,
                                                         Object value) {
        this.name = name;
        this.value = value;
        return this;
    }

    public UpdateTaskVariablePayload build() {
        return new UpdateTaskVariablePayload(taskId,
                                             name,
                                             value);
    }
}
