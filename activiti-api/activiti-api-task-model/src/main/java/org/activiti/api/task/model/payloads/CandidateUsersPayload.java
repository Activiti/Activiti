/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
