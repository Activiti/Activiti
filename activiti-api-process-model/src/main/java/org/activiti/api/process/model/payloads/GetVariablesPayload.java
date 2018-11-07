package org.activiti.api.process.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class GetVariablesPayload implements Payload {

    private String id;
    private String processInstanceId;

    public GetVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetVariablesPayload(String processInstanceId) {
        this();
        this.processInstanceId = processInstanceId;
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
}
