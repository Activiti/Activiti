package org.activiti.runtime.api.model.payloads;

import java.util.Set;

public class GetProcessInstancesPayload {

    private Set<String> processDefinitionKeys;
    private String businessKey;


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
