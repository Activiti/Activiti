package org.activiti.api.process.model.payloads;

import org.activiti.api.model.common.Payload;

import java.util.UUID;


public class SuspendProcessPayload implements Payload {

    private String id;
    private String processInstanceId;

    public SuspendProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    public SuspendProcessPayload(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
