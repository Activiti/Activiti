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

package org.activiti.runtime.api.model.impl;

import java.util.Date;

import org.activiti.engine.TaskService;
import org.activiti.runtime.api.model.FluentTask;
import org.activiti.runtime.api.model.builder.TaskCreator;

public class TaskCreatorImpl implements TaskCreator {

    private final TaskService taskService;
    private final APITaskConverter taskConverter;

    private String name;
    private String description;
    private Integer priority;
    private Date dueDate;
    private String assignee;
    private String parentTaskId;

    public TaskCreatorImpl(TaskService taskService,
                           APITaskConverter taskConverter) {
        this.taskService = taskService;
        this.taskConverter = taskConverter;
    }

    public TaskCreatorImpl(TaskService taskService,
                           APITaskConverter taskConverter,
                           String parentTaskId) {
        this.taskService = taskService;
        this.taskConverter = taskConverter;
        this.parentTaskId = parentTaskId;
    }

    @Override
    public TaskCreator name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TaskCreator description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public TaskCreator priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public TaskCreator dueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    @Override
    public TaskCreator assignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    @Override
    public FluentTask create() {
        final org.activiti.engine.task.Task task = taskService.newTask();
        task.setName(name);
        task.setDescription(description);
        task.setDueDate(dueDate);
        if (priority != null) {
            task.setPriority(priority);
        }

        task.setAssignee(assignee);
        task.setParentTaskId(parentTaskId);
        taskService.saveTask(task);
        return taskConverter.from(task);
    }
}
