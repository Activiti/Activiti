package org.activiti.runtime.api.model.payloads;

import java.util.Map;

public class SignalPayload {

    private String name;
    private Map<String, Object> variables;

    public SignalPayload() {
    }

    public SignalPayload(String name,
                         Map<String, Object> variables) {
        this.name = name;
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
