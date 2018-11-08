package org.activiti.api.process.model.payloads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class SetProcessVariablesPayload implements Payload {

    private String id;
    private String processInstanceId;
    private Map<String, Object> variables = new HashMap<>();

    public SetProcessVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public SetProcessVariablesPayload(String processInstanceId,
                                      Map<String, Object> variables) {
        this();
        this.processInstanceId = processInstanceId;
        this.variables = variables;
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

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
