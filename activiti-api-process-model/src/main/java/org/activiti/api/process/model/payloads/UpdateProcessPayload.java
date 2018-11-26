package org.activiti.api.process.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class UpdateProcessPayload implements Payload {

    private String id;
    private String processInstanceId;
    private String processInstanceName;
    private String processInstanceDescription;
    private String businessKey;
    
    public UpdateProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public UpdateProcessPayload(String processInstanceId,
                               String processInstanceName,
                               String processInstanceDescription,
                               String businessKey) {
        this();
        this.processInstanceId = processInstanceId;
        this.processInstanceName = processInstanceName;
        this.processInstanceDescription = processInstanceDescription;
        this.businessKey = businessKey;
    }

    @Override
    public String getId() {
        return id;
    }
    
    public String getProcessInstanceId() {
        return processInstanceId;
    }
  
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceDescription() {
        return processInstanceDescription;
    }
    
    public void setProcessInstanceDescription(String processInstanceDescription) {
        this.processInstanceDescription = processInstanceDescription;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }
    
    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }
    
    public String getBusinessKey() {
        return businessKey;
    }
    
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }
}
