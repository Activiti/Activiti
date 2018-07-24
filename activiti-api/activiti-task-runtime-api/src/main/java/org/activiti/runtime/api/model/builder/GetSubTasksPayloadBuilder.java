package org.activiti.runtime.api.model.builder;

import org.activiti.runtime.api.model.payloads.GetSubTasksPayload;

public class GetSubTasksPayloadBuilder {

    private String parentTaskId;

    public GetSubTasksPayloadBuilder withParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
        return this;
    }

    public GetSubTasksPayload build() {
        return new GetSubTasksPayload(parentTaskId);
    }
}
