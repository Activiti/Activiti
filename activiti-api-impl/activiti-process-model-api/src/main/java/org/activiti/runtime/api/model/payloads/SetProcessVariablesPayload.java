package org.activiti.runtime.api.model.payloads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class SetProcessVariablesPayload implements Payload {

    private String id;
    private String processInstanceId;
    private Map<String, Object> variables = new HashMap<>();
    private boolean localOnly = false;

    public SetProcessVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public SetProcessVariablesPayload(String processInstanceId,
                                      Map<String, Object> variables,
                                      boolean localOnly) {
        this();
        this.processInstanceId = processInstanceId;
        this.variables = variables;
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
