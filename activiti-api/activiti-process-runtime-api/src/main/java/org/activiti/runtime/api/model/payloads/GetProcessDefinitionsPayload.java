package org.activiti.runtime.api.model.payloads;

import java.util.Set;

public class GetProcessDefinitionsPayload {

    private Set<String> processDefinitionKeys;

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public void setProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
    }
}
