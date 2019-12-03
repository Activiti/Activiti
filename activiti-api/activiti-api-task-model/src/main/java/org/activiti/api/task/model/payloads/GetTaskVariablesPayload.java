package org.activiti.api.task.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class GetTaskVariablesPayload implements Payload {

    private String id;
    private String taskId;
    public GetTaskVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetTaskVariablesPayload(String taskId) {
        this();
        this.taskId = taskId;
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
}
