package org.activiti.api.task.model.payloads;

import java.util.List;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class CandidateGroupsPayload implements Payload {

    private String id;
    private String taskId;
    private List<String> candidateGroups;

    public CandidateGroupsPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public CandidateGroupsPayload(String taskId,
                                  List<String> candidateGroups) {
        this();
        this.taskId=taskId;
        this.candidateGroups = candidateGroups;
    }

    @Override
    public String getId() {
        return id;
    }

     public List<String> getCandidateGroups() {
        return candidateGroups;
    }

    public void setCandidateGroups(List<String> candidateGroups) {
        this.candidateGroups = candidateGroups;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
