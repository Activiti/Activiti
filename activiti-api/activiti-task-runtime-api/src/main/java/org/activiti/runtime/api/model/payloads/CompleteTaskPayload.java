package org.activiti.runtime.api.model.payloads;

import java.util.Map;

public class CompleteTaskPayload {

    private String taskId;
    private Map<String, Object> variables;

    public CompleteTaskPayload() {
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
