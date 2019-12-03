package org.activiti.api.task.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class UpdateTaskVariablePayload implements Payload {

    private String id;
    private String taskId;
    private String name;
    private Object value;

    public UpdateTaskVariablePayload() {
        this.id = UUID.randomUUID().toString();
    }

    public UpdateTaskVariablePayload(String taskId,
                                     String name,
                                     Object value) {
        this();
        this.taskId = taskId;
        this.name = name;
        this.value = value;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
