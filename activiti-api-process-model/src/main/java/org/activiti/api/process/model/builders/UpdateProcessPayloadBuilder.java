package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;

public class UpdateProcessPayloadBuilder {

    private String processInstanceId;
    private String name;
    private String description;
    private String businessKey;

    public UpdateProcessPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public UpdateProcessPayloadBuilder withDescription(String description) {
        this.description = description;
        return this;
    } 
    
    public UpdateProcessPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public UpdateProcessPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }
    
    public UpdateProcessPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        this.businessKey = processInstance.getBusinessKey();
        this.name = processInstance.getName();
        return this;
    } 


    public UpdateProcessPayload build() {
        return new UpdateProcessPayload(processInstanceId,
                name,
                description,
                businessKey);
    }
}
