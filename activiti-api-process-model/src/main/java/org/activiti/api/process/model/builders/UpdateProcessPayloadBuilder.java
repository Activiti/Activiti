package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.payloads.UpdateProcessPayload;

public class UpdateProcessPayloadBuilder {

    private String processInstanceName;
    private String processInstanceDescription;
    private String businessKey;

    public UpdateProcessPayloadBuilder withProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
        return this;
    }
    
    public UpdateProcessPayloadBuilder withProcessInstanceDescription(String withProcessInstanceDescription) {
        this.processInstanceDescription = withProcessInstanceDescription;
        return this;
    } 
    
    public UpdateProcessPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public UpdateProcessPayload build() {
        return new UpdateProcessPayload(processInstanceName,
                processInstanceDescription,
                businessKey);
    }
}
