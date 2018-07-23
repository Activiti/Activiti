package org.activiti.runtime.api.model.builder;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;

public class SuspendProcessPayloadBuilder {

    private String processInstanceId;
    private ProcessInstance processInstance;

    public SuspendProcessPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public SuspendProcessPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
        return this;
    }

    public SuspendProcessPayload build() {
        if (processInstance != null) {
            return new SuspendProcessPayload(processInstance.getId());
        }
        return new SuspendProcessPayload(processInstanceId);
    }
}
