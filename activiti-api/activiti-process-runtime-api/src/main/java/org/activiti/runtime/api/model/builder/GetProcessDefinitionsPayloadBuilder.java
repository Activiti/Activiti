package org.activiti.runtime.api.model.builder;

import java.util.Set;

import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;

public class GetProcessDefinitionsPayloadBuilder {

    private Set<String> processDefinitionKeys;


    public GetProcessDefinitionsPayloadBuilder withProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
        return this;
    }

    public GetProcessDefinitionsPayload build() {
        GetProcessDefinitionsPayload getProcessDefinitionsPayload = new GetProcessDefinitionsPayload();
        getProcessDefinitionsPayload.setProcessDefinitionKeys(processDefinitionKeys);
        return getProcessDefinitionsPayload;
    }
}
