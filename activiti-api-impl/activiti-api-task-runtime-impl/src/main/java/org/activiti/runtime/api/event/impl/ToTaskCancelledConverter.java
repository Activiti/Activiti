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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.model.impl.APITaskConverter;

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
        tasks = filterMultiInstances(internalEvent,
                                     tasks);
        TaskCancelledEvent event = null;
        if (tasks.size() == 1) {
            Task task = taskConverter.from(tasks.get(0));
            ((TaskImpl) task).setStatus(Task.TaskStatus.CANCELLED);
            event = new TaskCancelledImpl(task);
        }
        return Optional.ofNullable(event);
    }

    private List<org.activiti.engine.task.Task> filterMultiInstances(ActivitiActivityCancelledEvent internalEvent,
                                                                     List<org.activiti.engine.task.Task> tasks) {
        // in case of parallel multi-instance we need to also filter on execution id because more than will task can
        // be returned for same process instance id and task definition key.
        // Note that's not possible to always filter on execution id, because in some cases the execution related to
        // cancel action is not the the task itself. For instance, boundary events.
        if (tasks.size() > 1) {
            tasks = tasks
                    .stream()
                    .filter(task -> Objects.equals(task.getExecutionId(),
                                                   internalEvent.getExecutionId()))
                    .collect(Collectors.toList());
        }
        return tasks;
    }
}
