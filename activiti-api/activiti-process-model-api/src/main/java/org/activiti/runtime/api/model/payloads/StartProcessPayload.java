package org.activiti.runtime.api.model.payloads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class StartProcessPayload implements Payload {

    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processInstanceName;
    private String businessKey;
    private Map<String, Object> variables = new HashMap<>();

    public StartProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public StartProcessPayload(String processDefinitionId,
                               String processDefinitionKey,
                               String processInstanceName,
                               String businessKey,
                               Map<String, Object> variables) {
        this();
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.processInstanceName = processInstanceName;
        this.businessKey = businessKey;
        this.variables = variables;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }

    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }
}
