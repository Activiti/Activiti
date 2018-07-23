package org.activiti.runtime.api.model.payloads;

public class DeleteProcessPayload {

    private String processInstanceId;
    private String reason;

    public DeleteProcessPayload() {
    }

    public DeleteProcessPayload(String processInstanceId,
                                String reason) {
        this.processInstanceId = processInstanceId;
        this.reason = reason;
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
