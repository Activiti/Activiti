package org.activiti.runtime.api.model.payloads;

import java.util.Map;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class CompleteTaskPayload implements Payload {

    private String id;
    private String taskId;
    private Map<String, Object> variables;

    public CompleteTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public CompleteTaskPayload(String taskId,
                               Map<String, Object> variables) {
        this();
        this.taskId = taskId;
        this.variables = variables;
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

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
