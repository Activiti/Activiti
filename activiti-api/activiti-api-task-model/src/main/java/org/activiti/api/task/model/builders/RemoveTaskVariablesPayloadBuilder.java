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

import java.util.Date;

import org.activiti.api.task.model.payloads.UpdateTaskPayload;

public class RemoveTaskVariablesPayloadBuilder {

    private String taskId;
    private String name;
    private String description;
    private Date dueDate;
    private int priority;
    private String assignee;

    public RemoveTaskVariablesPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public RemoveTaskVariablesPayloadBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public UpdateTaskPayload build() {
        return new UpdateTaskPayload();
    }
}
