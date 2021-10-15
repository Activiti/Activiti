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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.runtime.event.impl.RuntimeEventImpl;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.TaskUpdatedEvent;

public class TaskUpdatedEventImpl
    extends RuntimeEventImpl<Task, TaskRuntimeEvent.TaskEvents>
    implements TaskUpdatedEvent {

    public TaskUpdatedEventImpl(Task entity) {
        super(entity);
    }

    @Override
    public TaskRuntimeEvent.TaskEvents getEventType() {
        return TaskRuntimeEvent.TaskEvents.TASK_UPDATED;
    }
}
