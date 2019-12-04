package org.activiti.api.process.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.process.model.payloads.SignalPayload;

public class SignalPayloadBuilder {

    private String name;
    private Map<String, Object> variables;

    public SignalPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SignalPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public SignalPayloadBuilder withVariable(String name,
                                             Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public SignalPayload build() {
        return new SignalPayload(name,
                                 this.variables);
    }
}
