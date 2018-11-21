package org.activiti.api.process.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class UpdateProcessPayload implements Payload {

    private String id;
    private String processInstanceName;
    private String processInstanceDescription;
    private String businessKey;
    
    public UpdateProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public UpdateProcessPayload(String processInstanceName,
                               String processInstanceDescription,
                               String businessKey) {
        this();
        this.processInstanceName = processInstanceName;
        this.processInstanceDescription = processInstanceDescription;
        this.businessKey = businessKey;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }
    
    public String getProcessInstanceDescription() {
        return processInstanceDescription;
    }
    
    public String getBusinessKey() {
        return businessKey;
    }

    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }
    
    public void setProcessInstanceDescription(String processInstanceDescription) {
        this.processInstanceDescription = processInstanceDescription;
    }
    
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }
}
