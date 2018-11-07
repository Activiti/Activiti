package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.GetVariablesPayload;

public class GetVariablesPayloadBuilder {

    private String processInstanceId;

    public GetVariablesPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public GetVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }

    public GetVariablesPayload build() {
        return new GetVariablesPayload(processInstanceId);
    }
}
