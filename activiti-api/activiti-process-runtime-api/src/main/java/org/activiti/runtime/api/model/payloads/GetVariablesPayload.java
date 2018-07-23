package org.activiti.runtime.api.model.payloads;

public class GetVariablesPayload {

    private String processInstanceId;
    private boolean localOnly = false;

    public GetVariablesPayload() {
    }

    public GetVariablesPayload(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public GetVariablesPayload(String processInstanceId,
                               boolean localOnly) {
        this.processInstanceId = processInstanceId;
        this.localOnly = localOnly;
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
