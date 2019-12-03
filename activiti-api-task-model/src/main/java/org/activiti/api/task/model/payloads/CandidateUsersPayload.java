package org.activiti.api.task.model.payloads;

import java.util.List;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class CandidateUsersPayload implements Payload {

    private String id;
    private String taskId;
    private List<String> candidateUsers;

    public CandidateUsersPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public CandidateUsersPayload(String taskId,
                                 List<String> candidateUsers) {
        this();
        this.setTaskId(taskId);
        this.candidateUsers = candidateUsers;
    }

    @Override
    public String getId() {
        return id;
    }

     public List<String> getCandidateUsers() {
        return candidateUsers;
    }

    public void setCandidateUsers(List<String> candidateUsers) {
        this.candidateUsers = candidateUsers;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
