package org.activiti.api.task.model.builders;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.task.model.payloads.GetTasksPayload;

public class GetTasksPayloadBuilder {

    private String assignee;
    private List<String> groups;
    private String processInstanceId;
    private String parentTaskId;

    public GetTasksPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public GetTasksPayloadBuilder withGroups(List<String> groups) {
        this.groups = groups;
        return this;
    }

    public GetTasksPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public GetTasksPayloadBuilder withParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
        return this;
    }

    public GetTasksPayloadBuilder withGroup(String group) {
        if (this.groups == null) {
            this.groups = new ArrayList<>();
        }
        this.groups.add(group);
        return this;
    }

    public GetTasksPayload build() {
        return new GetTasksPayload(assignee,
                                   groups,
                                   processInstanceId,
                                   parentTaskId);
    }
}
