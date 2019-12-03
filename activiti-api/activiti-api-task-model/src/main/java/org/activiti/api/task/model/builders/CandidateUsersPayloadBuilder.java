package org.activiti.api.task.model.builders;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.task.model.payloads.CandidateUsersPayload;

public class CandidateUsersPayloadBuilder {

    private String taskId;
    private List<String> candidateUsers = new ArrayList<>();
 
    public CandidateUsersPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public CandidateUsersPayloadBuilder withCandidateUsers(List<String> candidateUsers) {
        if (candidateUsers == null) {
            candidateUsers = new ArrayList<>();
        }
        this.candidateUsers = candidateUsers;
        return this;
    }

    public CandidateUsersPayloadBuilder withCandidateUser(String candidateUser) {
        this.candidateUsers.add(candidateUser);
        return this;
    }

    public CandidateUsersPayload build() {
        return new CandidateUsersPayload(taskId,
                                         candidateUsers);
    }
}
