package org.activiti.runtime.api.model.builder;

import java.util.HashMap;
import java.util.Map;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.GetVariablesPayload;

public class SetVariablesPayloadBuilder {

    private String processInstanceId;
    private ProcessInstance processInstance;
    private boolean localOnly;
    private Map<String, Object> variables = new HashMap<>();

    public SetVariablesPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public SetVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
        return this;
    }

    public SetVariablesPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public SetVariablesPayloadBuilder withVariable(String name,
                                                   Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public SetVariablesPayloadBuilder localOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public GetVariablesPayload build() {
        if (processInstance != null) {
            return new GetVariablesPayload(processInstance.getId(),
                                           localOnly);
        }
        return new GetVariablesPayload(processInstanceId,
                                       localOnly);
    }
}
