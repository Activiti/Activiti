package org.activiti.runtime.api.model.payloads;

import java.util.Set;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class GetProcessInstancesPayload implements Payload {

    private String id;
    private Set<String> processDefinitionKeys;
    private String businessKey;

    public GetProcessInstancesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public void setProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
    }
}
