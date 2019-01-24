package org.activiti.api.process.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class UpdateProcessPayload implements Payload {

    private String id;
    private String processInstanceId;
    private String name;
    private String description;
    private String businessKey;
    
    public UpdateProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public UpdateProcessPayload(String processInstanceId,
                               String name,
                               String description,
                               String businessKey) {
        this();
        this.processInstanceId = processInstanceId;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }
}
