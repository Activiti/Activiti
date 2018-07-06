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

package org.activiti.runtime.api.impl;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.conf.TaskRuntimeConfiguration;
import org.activiti.runtime.api.model.FluentTask;
import org.activiti.runtime.api.model.builder.TaskCreator;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.TaskCreatorImpl;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.TaskFilter;
import org.activiti.runtime.api.query.impl.PageImpl;

public class TaskRuntimeImpl implements TaskRuntime {

    private final TaskService taskService;

    private final APITaskConverter taskConverter;

    private final TaskRuntimeConfiguration configuration;

    public TaskRuntimeImpl(TaskService taskService,
                           APITaskConverter taskConverter,
                           TaskRuntimeConfiguration configuration) {
        this.taskService = taskService;
        this.taskConverter = taskConverter;
        this.configuration = configuration;
    }

    @Override
    public TaskRuntimeConfiguration configuration() {
        return configuration;
    }

    @Override
    public TaskCreator createTaskWith() {
        return new TaskCreatorImpl(taskService, taskConverter);
    }

    @Override
    public FluentTask task(String taskId) {
        Task internalTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (internalTask == null) {
            throw new NotFoundException("Unable to find task for the given id: " + taskId);
        }
        return taskConverter.from(internalTask);
    }

    @Override
    public Page<FluentTask> tasks(Pageable pageable) {
        return tasks(pageable,
                     null);
    }

    @Override
    public Page<FluentTask> tasks(Pageable pageable,
                                  TaskFilter filter) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        if (filter != null) {
            if (filter.getAssigneeId() != null) {
                taskQuery = taskQuery.taskCandidateOrAssigned(filter.getAssigneeId(),
                                                              filter.getGroups());
            }
            if (filter.getProcessInstanceId() != null) {
                taskQuery = taskQuery.processInstanceId(filter.getProcessInstanceId());
            }
        }
        List<FluentTask> tasks = taskConverter.from(taskQuery.listPage(pageable.getStartIndex(),
                                                                       pageable.getMaxItems()));
        return new PageImpl<>(tasks,
                              Math.toIntExact(taskQuery.count()));
    }
}
