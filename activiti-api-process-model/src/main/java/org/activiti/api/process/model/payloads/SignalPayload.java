package org.activiti.api.process.model.payloads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class SignalPayload implements Payload {

    private String id;
    private String name;
    private Map<String, Object> variables = new HashMap<>();

    public SignalPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public SignalPayload(String name,
                         Map<String, Object> variables) {
        this();
        this.name = name;
        this.variables = variables;
    }

    @Override
    public String getId() {
        return id;
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
