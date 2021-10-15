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

import org.activiti.api.task.model.payloads.AssignTaskPayload;

public class AssignTaskPayloadBuilder {

    private String taskId;
    private String assignee;

    public AssignTaskPayloadBuilder withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public AssignTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public AssignTaskPayload build() {
        return new AssignTaskPayload(taskId, assignee);
    }
}
