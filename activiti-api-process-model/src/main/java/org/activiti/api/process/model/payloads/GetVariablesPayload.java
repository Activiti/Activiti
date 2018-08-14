package org.activiti.api.process.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class GetVariablesPayload implements Payload {

    private String id;
    private String processInstanceId;
    private boolean localOnly = false;

    public GetVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetVariablesPayload(String processInstanceId) {
        this();
        this.processInstanceId = processInstanceId;
    }

    public GetVariablesPayload(String processInstanceId,
                               boolean localOnly) {
        this(processInstanceId);
        this.localOnly = localOnly;
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

    public boolean isLocalOnly() {
        return localOnly;
    }

    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }
}
