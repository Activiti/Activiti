package org.activiti.runtime.api.model.payloads;

import java.util.List;
import java.util.UUID;

import org.activiti.runtime.api.Payload;

public class GetTasksPayload implements Payload {

    private String id;
    private String assigneeId;
    private List<String> groups;
    private String processInstanceId;

    public GetTasksPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetTasksPayload(String assigneeId,
                           List<String> groups,
                           String processInstanceId) {
        this();
        this.assigneeId = assigneeId;
        this.groups = groups;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
