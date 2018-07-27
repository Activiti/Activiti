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

import java.util.List;
import java.util.Optional;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.runtime.api.event.TaskCancelled;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.TaskImpl;

public class ToTaskCancelledConverter implements EventConverter<TaskCancelled, ActivitiActivityCancelledEvent> {

    private APITaskConverter taskConverter;

    private TaskService taskService;

    public ToTaskCancelledConverter(APITaskConverter taskConverter,
                                    TaskService taskService) {
        this.taskConverter = taskConverter;
        this.taskService = taskService;
    }

    @Override
    public Optional<TaskCancelled> from(ActivitiActivityCancelledEvent internalEvent) {
        List<org.activiti.engine.task.Task> tasks = taskService.createTaskQuery()
                .processInstanceId(internalEvent.getProcessInstanceId())
                .taskDefinitionKey(internalEvent.getActivityId())
                .list();
        TaskCancelled event = null;
        if (!tasks.isEmpty()) {
            Task task = taskConverter.from(tasks.get(0));
            ((TaskImpl) task).setStatus(org.activiti.runtime.api.model.Task.TaskStatus.CANCELLED);
            event = new TaskCancelledImpl(task);
        }
        return Optional.ofNullable(event);
    }
}
