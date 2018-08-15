package org.activiti.runtime.api.model.payloads;

import java.util.Set;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class GetProcessDefinitionsPayload implements Payload {

    private String id;
    private String processDefinitionId;
    private Set<String> processDefinitionKeys;

    public GetProcessDefinitionsPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetProcessDefinitionsPayload(String processDefinitionId,
                                        Set<String> processDefinitionKeys) {
        this();
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKeys = processDefinitionKeys;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public boolean hasDefinitionKeys() {
        return processDefinitionKeys != null && !processDefinitionKeys.isEmpty();
    }

    public void setProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
    }
}
