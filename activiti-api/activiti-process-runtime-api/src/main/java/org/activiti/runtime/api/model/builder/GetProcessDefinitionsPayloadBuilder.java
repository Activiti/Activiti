package org.activiti.runtime.api.model.builder;

import java.util.HashSet;
import java.util.Set;

import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;

public class GetProcessDefinitionsPayloadBuilder {

    private String processDefinitionId;
    private Set<String> processDefinitionKeys;

    public GetProcessDefinitionsPayloadBuilder withProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
        return this;
    }

    public GetProcessDefinitionsPayloadBuilder withProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public GetProcessDefinitionsPayloadBuilder withProcessDefinitionKey(String processDefinitionKey) {
        if (processDefinitionKeys == null) {
            processDefinitionKeys = new HashSet<>();
        }
        processDefinitionKeys.add(processDefinitionKey);
        return this;
    }

    public GetProcessDefinitionsPayload build() {
        return new GetProcessDefinitionsPayload(processDefinitionId, processDefinitionKeys);
    }
}
