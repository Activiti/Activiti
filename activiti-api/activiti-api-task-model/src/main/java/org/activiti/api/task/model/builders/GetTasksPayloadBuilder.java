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
