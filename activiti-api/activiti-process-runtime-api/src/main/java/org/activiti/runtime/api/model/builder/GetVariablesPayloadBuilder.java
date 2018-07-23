package org.activiti.runtime.api.model.builder;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.GetVariablesPayload;

public class GetVariablesPayloadBuilder {

    private String processInstanceId;
    private ProcessInstance processInstance;
    private boolean localOnly;

    public GetVariablesPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public GetVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
        return this;
    }


    public GetVariablesPayloadBuilder localOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public GetVariablesPayload build() {
        if (processInstance != null) {
            return new GetVariablesPayload(processInstance.getId(), localOnly);
        }
        return new GetVariablesPayload(processInstanceId, localOnly);
    }
}
