package org.activiti.runtime.api.model.payloads;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class GetProcessInstancesPayload implements Payload {

    private String id;
    private Set<String> processDefinitionKeys = new HashSet<>();
    private String businessKey;
    private boolean suspendedOnly;
    private boolean activeOnly;

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

    public boolean isSuspendedOnly() {
        return suspendedOnly;
    }

    public void setSuspendedOnly(boolean suspendedOnly) {
        this.suspendedOnly = suspendedOnly;
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    public void setActiveOnly(boolean activeOnly) {
        this.activeOnly = activeOnly;
    }
}
