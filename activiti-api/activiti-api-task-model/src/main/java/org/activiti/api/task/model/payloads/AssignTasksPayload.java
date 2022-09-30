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

package org.activiti.api.task.model.payloads;

import org.activiti.api.model.shared.Payload;

import java.util.List;
import java.util.UUID;

public class AssignTasksPayload implements Payload {

    private String id;
    private List<String> taskIds;
    private String assignee;

    public AssignTasksPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public AssignTasksPayload(List<String> taskIds,
                              String assignee) {
        this();
        this.taskIds = taskIds;
        this.assignee = assignee;
    }

    @Override
    public String getId() {
        return id;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
