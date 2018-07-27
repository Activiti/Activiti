package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;

public class ResumeProcessPayloadBuilder {

    private String processInstanceId;

    public ResumeProcessPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public ResumeProcessPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }

    public ResumeProcessPayload build() {
        return new ResumeProcessPayload(processInstanceId);
    }
}
