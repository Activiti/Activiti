package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;

public class DeleteProcessPayloadBuilder {

    private String processInstanceId;
    private String reason;

    public DeleteProcessPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public DeleteProcessPayloadBuilder withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public DeleteProcessPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }

    public DeleteProcessPayload build() {
        return new DeleteProcessPayload(processInstanceId,
                                        reason);
    }
}
