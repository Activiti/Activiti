package org.activiti.runtime.api.model.payloads;

import java.util.Map;

public class SetVariablesPayload {

    private String processInstanceId;
    private Map<String, Object> variables;
    private boolean localOnly = false;

    public SetVariablesPayload() {
    }

    public SetVariablesPayload(String processInstanceId,
                               Map<String, Object> variables,
                               boolean localOnly) {
        this.processInstanceId = processInstanceId;
        this.variables = variables;
        this.localOnly = localOnly;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
