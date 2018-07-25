package org.activiti.runtime.api.model.payloads;

import java.util.Map;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class SetTaskVariablesPayload implements Payload {

    private String id;
    private String taskId;
    private Map<String, Object> variables;
    private boolean localOnly;

    public SetTaskVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public SetTaskVariablesPayload(String taskId,
                                   Map<String, Object> variables,
                                   boolean localOnly) {
        this();
        this.taskId = taskId;
        this.variables = variables;
        this.localOnly = localOnly;
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

    public boolean isLocalOnly() {
        return localOnly;
    }

    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }
}
