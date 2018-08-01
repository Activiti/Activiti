package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.GetVariablesPayload;

public class GetVariablesPayloadBuilder {

    private String processInstanceId;
    private boolean localOnly = false;

    public GetVariablesPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public GetVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }


    public GetVariablesPayloadBuilder localOnly() {
        this.localOnly = true;
        return this;
    }

    public GetVariablesPayload build() {
        return new GetVariablesPayload(processInstanceId, localOnly);
    }
}
