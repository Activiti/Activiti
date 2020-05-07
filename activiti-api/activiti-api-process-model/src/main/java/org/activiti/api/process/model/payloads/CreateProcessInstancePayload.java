package org.activiti.api.process.model.payloads;

import java.util.UUID;
import org.activiti.api.model.shared.Payload;

public class CreateProcessInstancePayload implements Payload {
    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String name;
    private String businessKey;

    public CreateProcessInstancePayload() {
        this.id = UUID.randomUUID().toString();
    }

    public CreateProcessInstancePayload(String processDefinitionId, String processDefinitionKey,
        String name, String businessKey) {
        this();
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.name = name;
        this.businessKey = businessKey;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusinessKey() {
        return businessKey;
    }
}
