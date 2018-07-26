package org.activiti.runtime.api.model.builders;

import java.util.HashSet;
import java.util.Set;

import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;

public class GetProcessInstancesPayloadBuilder {

    private String businessKey;
    private Set<String> processDefinitionKeys;

    public GetProcessInstancesPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public GetProcessInstancesPayloadBuilder withProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
        return this;
    }

    public GetProcessInstancesPayloadBuilder withProcessDefinitionKey(String processDefinitionKey) {
        if (processDefinitionKeys == null) {
            processDefinitionKeys = new HashSet<>();
        }
        processDefinitionKeys.add(processDefinitionKey);
        return this;
    }

    public GetProcessInstancesPayload build() {
        GetProcessInstancesPayload getProcessInstancesPayload = new GetProcessInstancesPayload();
        getProcessInstancesPayload.setBusinessKey(businessKey);
        getProcessInstancesPayload.setProcessDefinitionKeys(processDefinitionKeys);
        return getProcessInstancesPayload;
    }
}
