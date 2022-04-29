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
