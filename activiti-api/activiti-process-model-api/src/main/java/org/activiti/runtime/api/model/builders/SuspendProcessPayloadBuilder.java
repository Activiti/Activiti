package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;

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
