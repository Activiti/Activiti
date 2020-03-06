package org.activiti.api.process.model.builders;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.RemoveProcessVariablesPayload;

public class RemoveVariablesPayloadBuilder {

    private String processInstanceId;
    private List<String> variableNames = new ArrayList<>();

    public RemoveVariablesPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public RemoveVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }

    public RemoveVariablesPayloadBuilder withVariableNames(String variableName) {
        if (variableNames == null) {
            variableNames = new ArrayList<>();
        }
        variableNames.add(variableName);
        return this;
    }

    public RemoveVariablesPayloadBuilder withVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
        return this;
    }

    public RemoveProcessVariablesPayload build() {
        return new RemoveProcessVariablesPayload(processInstanceId,
                                                 variableNames);
    }
}
