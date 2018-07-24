package org.activiti.runtime.api.model.payloads;

import java.util.List;

public class RemoveProcessVariablesPayload {

    private String processInstanceId;
    private List<String> variableNames;
    private boolean localOnly;

    public RemoveProcessVariablesPayload() {
    }

    public RemoveProcessVariablesPayload(String processInstanceId,
                                         List<String> variableNames,
                                         boolean localOnly) {
        this.processInstanceId = processInstanceId;
        this.variableNames = variableNames;
        this.localOnly = localOnly;
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
