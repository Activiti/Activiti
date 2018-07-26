package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.payloads.ReleaseTaskPayload;

public class ReleaseTaskPayloadBuilder {

    private String taskId;

    public ReleaseTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }



    public ReleaseTaskPayload build() {
        return new ReleaseTaskPayload(taskId);
    }
}
