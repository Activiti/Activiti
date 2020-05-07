package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;

public class CreateProcessPayloadBuilder {

    private String processDefinitionId;
    private String processDefinitionKey;
    private String name;

    public CreateProcessPayloadBuilder() {
    }

    public CreateProcessPayloadBuilder(String processDefinitionId, String processDefinitionKey, String name) {
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.name = name;
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

    public CreateProcessInstancePayload build() {
        return new CreateProcessInstancePayload(processDefinitionId,
            processDefinitionKey,
            name);
    }
}
