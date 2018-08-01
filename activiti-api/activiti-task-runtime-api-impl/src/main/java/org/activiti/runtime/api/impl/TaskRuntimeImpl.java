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
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.conf.TaskRuntimeConfiguration;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.VariableInstance;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.model.impl.TaskImpl;
import org.activiti.runtime.api.model.payloads.ClaimTaskPayload;
import org.activiti.runtime.api.model.payloads.CompleteTaskPayload;
import org.activiti.runtime.api.model.payloads.CreateTaskPayload;
import org.activiti.runtime.api.model.payloads.DeleteTaskPayload;
import org.activiti.runtime.api.model.payloads.GetTaskVariablesPayload;
import org.activiti.runtime.api.model.payloads.GetTasksPayload;
import org.activiti.runtime.api.model.payloads.ReleaseTaskPayload;
import org.activiti.runtime.api.model.payloads.SetTaskVariablesPayload;
import org.activiti.runtime.api.model.payloads.UpdateTaskPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.impl.PageImpl;

public class TaskRuntimeImpl implements TaskRuntime {

    private final TaskService taskService;

    private final APITaskConverter taskConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final TaskRuntimeConfiguration configuration;

    public TaskRuntimeImpl(TaskService taskService,
                           APITaskConverter taskConverter,
                           APIVariableInstanceConverter variableInstanceConverter,
                           TaskRuntimeConfiguration configuration) {
        this.taskService = taskService;
        this.taskConverter = taskConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.configuration = configuration;
    }

    @Override
    public TaskRuntimeConfiguration configuration() {
        return configuration;
    }

    @Override
    public Task task(String taskId) {
        org.activiti.engine.task.Task internalTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (internalTask == null) {
            throw new NotFoundException("Unable to find task for the given id: " + taskId);
        }
        return taskConverter.from(internalTask);
    }

    @Override
    public Page<Task> tasks(Pageable pageable) {
        return tasks(pageable,
                     null);
    }

    @Override
    public Page<Task> tasks(Pageable pageable,
                            GetTasksPayload getTasksPayload) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        if (getTasksPayload != null) {
            if (getTasksPayload.getAssigneeId() != null) {
                taskQuery = taskQuery.taskCandidateOrAssigned(getTasksPayload.getAssigneeId(),
                                                              getTasksPayload.getGroups());
            }
            if (getTasksPayload.getProcessInstanceId() != null) {
                taskQuery = taskQuery.processInstanceId(getTasksPayload.getProcessInstanceId());
            }
            if (getTasksPayload.getParentTaskId() != null) {
                taskQuery = taskQuery.taskParentTaskId(getTasksPayload.getParentTaskId());
            }
        }
        List<Task> tasks = taskConverter.from(taskQuery.listPage(pageable.getStartIndex(),
                                                                 pageable.getMaxItems()));
        return new PageImpl<>(tasks,
                              Math.toIntExact(taskQuery.count()));
    }

    @Override
    public List<VariableInstance> variables(GetTaskVariablesPayload getTaskVariablesPayload) {
        if (getTaskVariablesPayload.isLocalOnly()) {
            return variableInstanceConverter.from(taskService.getVariableInstancesLocal(getTaskVariablesPayload.getTaskId()).values());
        } else {
            return variableInstanceConverter.from(taskService.getVariableInstances(getTaskVariablesPayload.getTaskId()).values());
        }
    }

    @Override
    public Task complete(CompleteTaskPayload completeTaskPayload) {
        //@TODO: not the most efficient way to return the just completed task, improve
        //      we might need to create an empty shell with the task ID and Status only
        Task task = task(completeTaskPayload.getTaskId());
        TaskImpl competedTaskData = new TaskImpl(task.getId(), task.getName(), Task.TaskStatus.COMPLETED);
        taskService.complete(completeTaskPayload.getTaskId(),
                             completeTaskPayload.getVariables());
        return competedTaskData;
    }

    @Override
    public Task claim(ClaimTaskPayload claimTaskPayload) {
        taskService.claim(claimTaskPayload.getTaskId(),
                          claimTaskPayload.getAssignee());
        return task(claimTaskPayload.getTaskId());
    }

    @Override
    public Task release(ReleaseTaskPayload releaseTaskPayload) {
        taskService.unclaim(releaseTaskPayload.getTaskId());
        return task(releaseTaskPayload.getTaskId());
    }

    @Override
    public Task update(UpdateTaskPayload updateTaskPayload) {
        org.activiti.engine.task.Task internalTask = getInternalTask(updateTaskPayload.getTaskId());
        if (updateTaskPayload.getTaskName() != null) {
            internalTask.setName(updateTaskPayload.getTaskName());
        }
        if (updateTaskPayload.getDescription() != null) {
            internalTask.setDescription(updateTaskPayload.getDescription());
        }
        if (Integer.valueOf(updateTaskPayload.getPriority()) != null) {
            internalTask.setPriority(updateTaskPayload.getPriority());
        }
        if (updateTaskPayload.getAssignee() != null) {
            internalTask.setAssignee(updateTaskPayload.getAssignee());
        }
        if (updateTaskPayload.getDueDate() != null) {
            internalTask.setDueDate(updateTaskPayload.getDueDate());
        }
        //@TODO: add check to see if something was changed before saving + add all other updateable values
        taskService.saveTask(internalTask);
        return task(updateTaskPayload.getTaskId());
    }

    @Override
    public Task delete(DeleteTaskPayload deleteTaskPayload) {
        //@TODO: not the most efficient way to return the just deleted task, improve
        //      we might need to create an empty shell with the task ID and Status only
        Task task = task(deleteTaskPayload.getTaskId());
        TaskImpl deletedTaskData = new TaskImpl(task.getId(), task.getName(), Task.TaskStatus.DELETED);
        taskService.deleteTask(deleteTaskPayload.getTaskId(),deleteTaskPayload.getReason(), true);
        return deletedTaskData;
    }

    @Override
    public Task create(CreateTaskPayload createTaskPayload) {
        org.activiti.engine.task.Task task = taskService.newTask();
        task.setName(createTaskPayload.getName());
        task.setDescription(createTaskPayload.getDescription());
        task.setDueDate(createTaskPayload.getDueDate());
        task.setPriority(createTaskPayload.getPriority());
        task.setAssignee(createTaskPayload.getAssignee());
        task.setParentTaskId(createTaskPayload.getParentTaskId());
        taskService.saveTask(task);
        return taskConverter.from(task);
    }

    @Override
    public void setVariables(SetTaskVariablesPayload setTaskVariablesPayload) {
        if (setTaskVariablesPayload.isLocalOnly()) {
            taskService.setVariablesLocal(setTaskVariablesPayload.getTaskId(),
                                          setTaskVariablesPayload.getVariables());
        } else {
            taskService.setVariables(setTaskVariablesPayload.getTaskId(),
                                     setTaskVariablesPayload.getVariables());
        }
    }

    private org.activiti.engine.task.Task getInternalTask(String taskId) {
        org.activiti.engine.task.Task internalTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (internalTask == null) {
            throw new NotFoundException("Unable to find task for the given id: " + taskId);
        }
        return internalTask;
    }
}
