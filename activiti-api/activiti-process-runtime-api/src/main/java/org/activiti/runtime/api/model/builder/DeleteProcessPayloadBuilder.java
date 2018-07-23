package org.activiti.runtime.api.model.builder;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;

public class DeleteProcessPayloadBuilder {

    private String processInstanceId;
    private ProcessInstance processInstance;
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
        this.processInstance = processInstance;
        return this;
    }

    public DeleteProcessPayload build() {
        if (processInstance != null) {
            return new DeleteProcessPayload(processInstance.getId(),
                                            reason);
        }
        return new DeleteProcessPayload(processInstanceId,
                                        reason);
    }
}
