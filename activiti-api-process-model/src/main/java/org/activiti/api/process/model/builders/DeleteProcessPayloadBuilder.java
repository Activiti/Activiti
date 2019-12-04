package org.activiti.api.process.model.builders;


import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;

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
