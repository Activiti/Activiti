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
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.model.FluentTask;
import org.activiti.runtime.api.model.VariableInstance;
import org.activiti.runtime.api.model.builder.CompleteTaskPayload;
import org.activiti.runtime.api.model.builder.TaskCreator;

public class FluentTaskImpl extends TaskImpl implements FluentTask {

    private final TaskService taskService;
    private final APIVariableInstanceConverter variableInstanceConverter;
    private final APITaskConverter taskConverter;

    public FluentTaskImpl(TaskService taskService,
                          APIVariableInstanceConverter variableInstanceConverter,
                          APITaskConverter taskConverter,
                          String id,
                          String name,
                          TaskStatus status) {
        super(id,
              name,
              status);
        this.taskService = taskService;
        this.variableInstanceConverter = variableInstanceConverter;
        this.taskConverter = taskConverter;
    }

    @Override
    public <T> void variable(String name,
                                               T value) {
        taskService.setVariable(getId(), name, value);
    }

    @Override
    public void variables(Map<String, Object> variables) {
        if (variables != null && !variables.isEmpty()) {
            taskService.setVariables(getId(), variables);
        }
    }

    @Override
    public <T> void localVariable(String name,
                                  T value) {
        taskService.setVariableLocal(getId(), name, value);
    }

    @Override
    public void localVariables(Map<String, Object> variables) {
        if (variables != null && !variables.isEmpty()) {
            taskService.setVariablesLocal(getId(), variables);
        }
    }

    @Override
    public List<VariableInstance> variables() {
        return variableInstanceConverter.from(taskService.getVariableInstances(getId()).values());
    }

    @Override
    public List<VariableInstance> localVariables() {
        return variableInstanceConverter.from(taskService.getVariableInstancesLocal(getId()).values());
    }

    @Override
    public void complete() {
        taskService.complete(getId());
    }

    @Override
    public CompleteTaskPayload completeWith() {
        return new CompleteTaskPayloadImpl(taskService,
                                           getId());
    }

    @Override
    public void claim(String username) {
        taskService.setAssignee(getId(),
                                username);
    }

    @Override
    public void release() {
        taskService.unclaim(getId());
    }

    @Override
    public void updateName(String name) {
        Task internalTask = getInternalTask();
        internalTask.setName(name);
        taskService.saveTask(internalTask);
        setName(name);
    }

    private Task getInternalTask() {
        Task internalTask = taskService.createTaskQuery().taskId(getId()).singleResult();
        if (internalTask == null) {
            throw new NotFoundException("Unable to find task for the given id: " + getId());
        }
        return internalTask;
    }

    @Override
    public void updateDescription(String description) {
        Task internalTask = getInternalTask();
        internalTask.setDescription(description);
        taskService.saveTask(internalTask);
        setDescription(description);
    }

    @Override
    public void updateDueDate(Date dueDate) {
        taskService.setDueDate(getId(), dueDate);
        setDueDate(dueDate);
    }

    @Override
    public void updatePriority(int priority) {
        taskService.setPriority(getId(), priority);
        setPriority(priority);
    }

    @Override
    public void updateParentTaskId(String parentTaskId) {
        Task internalTask = getInternalTask();
        internalTask.setParentTaskId(parentTaskId);
        taskService.saveTask(internalTask);
        setParentTaskId(parentTaskId);
    }

    @Override
    public void delete(String reason) {
        taskService.deleteTask(getId(), reason, true);
    }

    @Override
    public TaskCreator createSubTaskWith() {
        return new TaskCreatorImpl(taskService, taskConverter, getId());
    }

    @Override
    public List<FluentTask> subTasks() {
        return taskConverter.from(taskService.getSubTasks(getId()));
    }
}
