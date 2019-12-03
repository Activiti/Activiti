package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.ResumeProcessPayload;

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
