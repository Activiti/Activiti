package org.activiti.runtime.api.model.payloads;

import java.util.Map;

public class SuspendProcessPayload {
    private String processInstanceId;

    public SuspendProcessPayload() {
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
