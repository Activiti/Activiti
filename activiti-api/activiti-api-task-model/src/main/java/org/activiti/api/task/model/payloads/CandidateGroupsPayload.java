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
