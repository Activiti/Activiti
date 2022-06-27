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
package org.activiti.runtime.api.event.impl;

import java.util.Optional;

import org.activiti.api.task.model.Task;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.runtime.api.model.impl.APITaskConverter;

public class ToTaskCancelledConverter implements EventConverter<TaskCancelledEvent, ActivitiEntityEvent> {

    private APITaskConverter taskConverter;

    public ToTaskCancelledConverter(APITaskConverter taskConverter) {
        this.taskConverter = taskConverter;
    }

    @Override
    public Optional<TaskCancelledEvent> from(ActivitiEntityEvent internalEvent) {
        TaskCancelledEvent event = null;
        if (isTaskCancelled(internalEvent)) {
            TaskEntity taskEntity = (TaskEntity) internalEvent.getEntity();
            Task task = taskConverter.from(taskEntity, Task.TaskStatus.CANCELLED);
            String reason = internalEvent.getReason();
            event = new TaskCancelledImpl(task, reason);
        }
        return Optional.ofNullable(event);
    }

    private boolean isTaskCancelled(ActivitiEntityEvent internalEvent) {
        return internalEvent.getEntity() != null &&
                internalEvent.getEntity() instanceof TaskEntity &&
                ((TaskEntity) internalEvent.getEntity()).isCanceled();
    }
}
