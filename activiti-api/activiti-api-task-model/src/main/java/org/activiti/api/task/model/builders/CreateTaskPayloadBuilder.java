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
import java.util.Date;
import java.util.List;

import org.activiti.api.task.model.payloads.CreateTaskPayload;

public class CreateTaskPayloadBuilder {

    private String name;
    private String description;
    private Date dueDate;
    private int priority;
    private String assignee;
    private List<String> candidateGroups = new ArrayList<>();
    private List<String> candidateUsers = new ArrayList<>();
    private String parentTaskId;
    private String formKey;

    public CreateTaskPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreateTaskPayloadBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CreateTaskPayloadBuilder withDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public CreateTaskPayloadBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public CreateTaskPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public CreateTaskPayloadBuilder withParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
        return this;
    }

    public CreateTaskPayloadBuilder withFormKey(String formKey) {
        this.formKey = formKey;
        return this;
    }

    public CreateTaskPayloadBuilder withCandidateGroups(List<String> candidateGroups) {
        if (candidateGroups == null) {
            candidateGroups = new ArrayList<>();
        }
        this.candidateGroups = candidateGroups;
        return this;
    }

    public CreateTaskPayloadBuilder withCandidateGroup(String candidateGroups) {
        this.candidateGroups.add(candidateGroups);
        return this;
    }

    public CreateTaskPayloadBuilder withCandidateUsers(List<String> candidateUsers) {
        if (candidateUsers == null) {
            candidateUsers = new ArrayList<>();
        }
        this.candidateUsers = candidateUsers;
        return this;
    }

    public CreateTaskPayloadBuilder withCandidateUsers(String candidateUsers) {
        this.candidateUsers.add(candidateUsers);
        return this;
    }

    public CreateTaskPayload build() {
        return new CreateTaskPayload(name,
                description,
                dueDate,
                priority,
                assignee,
                candidateGroups,
                candidateUsers,
                parentTaskId,
                formKey);
    }
}
