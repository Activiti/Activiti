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

import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class DeleteTaskPayload implements Payload {

    private String id;
    private String taskId;
    private String reason;

    public DeleteTaskPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public DeleteTaskPayload(String taskId,
                             String reason) {
        this();
        this.taskId = taskId;
        this.reason = reason;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getReason() {
        return reason;
    }

    public boolean hasReason() {
        return reason != null;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
