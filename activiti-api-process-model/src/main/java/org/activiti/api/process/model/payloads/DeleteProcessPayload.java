package org.activiti.api.process.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;


public class DeleteProcessPayload implements Payload {

    private String id;
    private String processInstanceId;
    private String reason;

    public DeleteProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public DeleteProcessPayload(String processInstanceId,
                                String reason) {
        this();
        this.processInstanceId = processInstanceId;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
