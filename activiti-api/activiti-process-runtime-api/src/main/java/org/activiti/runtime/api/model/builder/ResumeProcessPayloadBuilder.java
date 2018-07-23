package org.activiti.runtime.api.model.builder;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;

public class ResumeProcessPayloadBuilder {

    private String processInstanceId;
    private ProcessInstance processInstance;

    public ResumeProcessPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public ResumeProcessPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
        return this;
    }

    public ResumeProcessPayload build() {
        if (processInstance != null) {
            return new ResumeProcessPayload(processInstance.getId());
        }
        return new ResumeProcessPayload(processInstanceId);
    }
}
