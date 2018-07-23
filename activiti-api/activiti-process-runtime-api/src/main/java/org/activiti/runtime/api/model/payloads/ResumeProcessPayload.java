package org.activiti.runtime.api.model.payloads;

public class ResumeProcessPayload {

    private String processInstanceId;

    public ResumeProcessPayload() {
    }

    public ResumeProcessPayload(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
