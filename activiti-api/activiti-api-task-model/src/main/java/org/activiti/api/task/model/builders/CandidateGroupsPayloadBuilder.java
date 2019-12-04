package org.activiti.api.task.model.builders;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.task.model.payloads.CandidateGroupsPayload;

public class CandidateGroupsPayloadBuilder {

    private String taskId;
    private List<String> candidateGroups = new ArrayList<>();
 
    public CandidateGroupsPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public CandidateGroupsPayloadBuilder withCandidateGroups(List<String> candidateGroups) {
        if (candidateGroups == null) {
            candidateGroups = new ArrayList<>();
        }
        this.candidateGroups = candidateGroups;
        return this;
    }

    public CandidateGroupsPayloadBuilder withCandidateGroup(String candidateGroup) {
        this.candidateGroups.add(candidateGroup);
        return this;
    }

    public CandidateGroupsPayload build() {
        return new CandidateGroupsPayload(taskId,
                                          candidateGroups);
    }
}
