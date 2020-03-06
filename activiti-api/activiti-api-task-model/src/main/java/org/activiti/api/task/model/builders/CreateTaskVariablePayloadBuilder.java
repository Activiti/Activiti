package org.activiti.api.task.model.builders;

import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;

public class CreateTaskVariablePayloadBuilder {

    private String taskId;
    private String name;
    private Object value;

    public CreateTaskVariablePayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public CreateTaskVariablePayloadBuilder withVariable(String name,
                                                         Object value) {
        this.name = name;
        this.value = value;
        return this;
    }

    public CreateTaskVariablePayload build() {
        return new CreateTaskVariablePayload(taskId,
                                             name,
                                             value);
    }
}
