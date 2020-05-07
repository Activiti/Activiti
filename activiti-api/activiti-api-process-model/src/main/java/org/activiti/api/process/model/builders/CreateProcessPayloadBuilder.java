package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;

public class CreateProcessPayloadBuilder {

    private String processDefinitionId;
    private String processDefinitionKey;
    private String name;
    private String businessKey;

    public CreateProcessPayloadBuilder() {
    }

    public CreateProcessPayloadBuilder withProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public CreateProcessPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreateProcessPayloadBuilder withProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
        return this;
    }

    public CreateProcessPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public CreateProcessInstancePayload build() {
        return new CreateProcessInstancePayload(processDefinitionId,
            processDefinitionKey,
            name,
            businessKey);
    }
}
