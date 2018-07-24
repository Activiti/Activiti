package org.activiti.runtime.api.model.payloads;

public class GetSubTasksPayload {

    private String parentTaskId;

    public GetSubTasksPayload() {
    }

    public GetSubTasksPayload(String parentTaskId) {

        this.parentTaskId = parentTaskId;
    }
    
    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
}
