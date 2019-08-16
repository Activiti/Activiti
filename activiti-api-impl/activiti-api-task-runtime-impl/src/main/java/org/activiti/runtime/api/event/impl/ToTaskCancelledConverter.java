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

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.model.impl.APITaskConverter;

import java.util.List;
import java.util.Optional;

public class ToTaskCancelledConverter implements EventConverter<TaskCancelledEvent, ActivitiActivityCancelledEvent> {

    private APITaskConverter taskConverter;

    private TaskService taskService;

    public ToTaskCancelledConverter(APITaskConverter taskConverter,
                                    TaskService taskService) {
        this.taskConverter = taskConverter;
        this.taskService = taskService;
    }

    @Override
    public Optional<TaskCancelledEvent> from(ActivitiActivityCancelledEvent internalEvent) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        if (internalEvent.getProcessInstanceId() != null) {
            taskQuery.processInstanceId(internalEvent.getProcessInstanceId());
            if (internalEvent.getActivityId() != null) {
                taskQuery.taskDefinitionKey(internalEvent.getActivityId());
            }
        } else {
            if (internalEvent.getExecutionId() != null) {
                //temporary workaround for stand alone tasks, task id is mapped as execution id
                taskQuery.taskId(internalEvent.getExecutionId());
            }
        }
        List<org.activiti.engine.task.Task> tasks = taskQuery.list();
        TaskCancelledEvent event = null;
        if (tasks.size() == 1) {
            Task task = taskConverter.from(tasks.get(0));
            ((TaskImpl) task).setStatus(Task.TaskStatus.CANCELLED);
            event = new TaskCancelledImpl(task);
        }
        return Optional.ofNullable(event);
    }
}
