package org.activiti.api.task.model.payloads;

import java.util.List;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class GetTasksPayload implements Payload {

    private String id;
    private String assigneeId;
    private List<String> groups;
    private String processInstanceId;
    private String parentTaskId;

    public GetTasksPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetTasksPayload(String assigneeId,
                           List<String> groups,
                           String processInstanceId,
                           String parentTaskId) {
        this();
        this.assigneeId = assigneeId;
        this.groups = groups;
        this.processInstanceId = processInstanceId;
        this.parentTaskId = parentTaskId;
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

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public boolean isStandalone() {
        return getProcessInstanceId() == null;
    }
}
