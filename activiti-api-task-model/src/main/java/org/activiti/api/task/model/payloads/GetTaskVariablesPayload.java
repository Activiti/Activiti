package org.activiti.api.task.model.payloads;

import java.util.Collection;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class GetTaskVariablesPayload implements Payload {

    private String id;
    private String taskId;
    private Collection<String> variableNames;

    public GetTaskVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetTaskVariablesPayload(String taskId,
                                   Collection<String> variableNames) {
        this();
        this.taskId = taskId;
        this.variableNames = variableNames;
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

    public Collection<String> getVariableNames() {
        return variableNames;
    }

    public void setVariableNames(Collection<String> variableNames) {
        this.variableNames = variableNames;
    }
}
