package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.SuspendProcessPayload;

public class SuspendProcessPayloadBuilder {

    private String processInstanceId;

    public SuspendProcessPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public SuspendProcessPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }

    public SuspendProcessPayload build() {
        return new SuspendProcessPayload(processInstanceId);
    }
}
