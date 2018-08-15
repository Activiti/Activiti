package org.activiti.runtime.api.model.payloads;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class RemoveProcessVariablesPayload implements Payload {

    private String id;
    private String processInstanceId;
    private List<String> variableNames = new ArrayList<>();
    private boolean localOnly;

    public RemoveProcessVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public RemoveProcessVariablesPayload(String processInstanceId,
                                         List<String> variableNames,
                                         boolean localOnly) {
        this();
        this.processInstanceId = processInstanceId;
        this.variableNames = variableNames;
        this.localOnly = localOnly;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public void setVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }
}
