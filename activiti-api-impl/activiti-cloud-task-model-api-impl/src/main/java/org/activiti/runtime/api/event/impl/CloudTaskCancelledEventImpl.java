/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.event.impl;

import org.activiti.runtime.api.event.CloudTaskCancelledEvent;
import org.activiti.runtime.api.event.TaskRuntimeEvent;
import org.activiti.runtime.api.model.Task;

public class CloudTaskCancelledEventImpl extends CloudRuntimeEventImpl<Task, TaskRuntimeEvent.TaskEvents>
        implements CloudTaskCancelledEvent {

    private String cause;

    public CloudTaskCancelledEventImpl() {
    }

    public CloudTaskCancelledEventImpl(Task task) {
        super(task);
        setEntityId(task.getId());
    }

    public CloudTaskCancelledEventImpl(Task task,
                                       String cause) {
        super(task);
        setEntityId(task.getId());
        this.cause = cause;
    }

    public CloudTaskCancelledEventImpl(String id,
                                       Long timestamp,
                                       Task task) {
        super(id,
              timestamp,
              task);
        setEntityId(task.getId());
    }

    @Override
    public TaskEvents getEventType() {
        return TaskEvents.TASK_CANCELLED;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
