/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.api.task.model.payloads.AssignTasksPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignTasksPayloadBuilder {

    private List<String> taskIds = new ArrayList<>();
    private String assignee;

    public AssignTasksPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public AssignTasksPayloadBuilder withTaskIds(List<String> taskIds) {
        this.taskIds = Objects.requireNonNullElseGet(taskIds, ArrayList::new);
        return this;
    }

    public AssignTasksPayloadBuilder withTaskId(String taskId) {
        this.taskIds.add(taskId);
        return this;
    }

    public AssignTasksPayload build() {
        return new AssignTasksPayload(taskIds, assignee);
    }
}
