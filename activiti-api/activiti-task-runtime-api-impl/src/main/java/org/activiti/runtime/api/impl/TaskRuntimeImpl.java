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
import java.util.Objects;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.conf.TaskRuntimeConfiguration;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.VariableInstance;
import org.activiti.runtime.api.model.builders.TaskPayloadBuilder;
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
import org.activiti.runtime.api.security.SecurityManager;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('ACTIVITI_USER')")
public class TaskRuntimeImpl implements TaskRuntime {

    private final TaskService taskService;

    private final APITaskConverter taskConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final TaskRuntimeConfiguration configuration;

    private final UserGroupManager userGroupManager;

    private final SecurityManager securityManager;

    public TaskRuntimeImpl(TaskService taskService,
                           UserGroupManager userGroupManager,
                           SecurityManager securityManager,
                           APITaskConverter taskConverter,
                           APIVariableInstanceConverter variableInstanceConverter,
                           TaskRuntimeConfiguration configuration) {
        this.taskService = taskService;
        this.userGroupManager = userGroupManager;
        this.securityManager = securityManager;
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
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        if (authenticatedUserId != null && !authenticatedUserId.isEmpty()) {
            List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
            List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
            org.activiti.engine.task.Task internalTask = taskService.createTaskQuery().taskCandidateOrAssigned(authenticatedUserId,
                                                                                                               userGroups).taskId(taskId).singleResult();
            if (internalTask == null) {
                throw new NotFoundException("Unable to find task for the given id: " + taskId + " for user: " + authenticatedUserId + " (with groups: " + userGroups + " & with roles: " + userRoles + ")");
            }
            return taskConverter.from(internalTask);
        }
        throw new IllegalStateException("There is no authenticated user, we need a user authenticated to find tasks");
    }

    @Override
    public Page<Task> tasks(Pageable pageable) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        if (authenticatedUserId != null && !authenticatedUserId.isEmpty()) {
            List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
            return tasks(pageable,
                         TaskPayloadBuilder.tasks().withAssignee(authenticatedUserId).withGroups(userGroups).build());
        }
        throw new IllegalStateException("You need an authenticated user to perform a task query");
    }

    @Override
    public Page<Task> tasks(Pageable pageable,
                            GetTasksPayload getTasksPayload) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        if (getTasksPayload == null) {
            getTasksPayload = TaskPayloadBuilder.tasks().build();
        }
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        if (authenticatedUserId != null && !authenticatedUserId.isEmpty()) {
            List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
            getTasksPayload.setAssigneeId(authenticatedUserId);
            getTasksPayload.setGroups(userGroups);
        } else {
            throw new IllegalStateException("You need an authenticated user to perform a task query");
        }
        taskQuery = taskQuery.or()
                .taskCandidateOrAssigned(getTasksPayload.getAssigneeId(),
                                         getTasksPayload.getGroups())
                .taskOwner(authenticatedUserId)
                .endOr();

        if (getTasksPayload.getProcessInstanceId() != null) {
            taskQuery = taskQuery.processInstanceId(getTasksPayload.getProcessInstanceId());
        }
        if (getTasksPayload.getParentTaskId() != null) {
            taskQuery = taskQuery.taskParentTaskId(getTasksPayload.getParentTaskId());
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
        Task task;
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        try {
            task = task(completeTaskPayload.getTaskId());
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot complete task" + completeTaskPayload.getTaskId() + " due he/she cannot access to the task");
        }
        // validate the the task does have an assignee
        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            throw new IllegalStateException("The task needs to be claimed before trying to complete it");
        }
        if (!task.getAssignee().equals(authenticatedUserId)) {
            throw new IllegalStateException("You cannot complete the task if you are not assigned to it");
        }
        TaskImpl competedTaskData = new TaskImpl(task.getId(),
                                                 task.getName(),
                                                 Task.TaskStatus.COMPLETED);
        taskService.complete(completeTaskPayload.getTaskId(),
                             completeTaskPayload.getVariables());
        return competedTaskData;
    }

    @Override
    public Task claim(ClaimTaskPayload claimTaskPayload) {
        // Validate that the task is visible by the currently authorized user
        Task task;
        try {
            task = task(claimTaskPayload.getTaskId());
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot claim task" + claimTaskPayload.getTaskId() + " due it is not a candidate for it");
        }
        // validate the the task doesn't have an assignee
        if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            throw new IllegalStateException("The task was already claimed, the assignee of this task needs to release it first for you to claim it");
        }

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        claimTaskPayload.setAssignee(authenticatedUserId);
        taskService.claim(claimTaskPayload.getTaskId(),
                          claimTaskPayload.getAssignee());

        return task(claimTaskPayload.getTaskId());
    }

    @Override
    public Task release(ReleaseTaskPayload releaseTaskPayload) {
        // Validate that the task is visible by the currently authorized user
        Task task;
        try {
            task = task(releaseTaskPayload.getTaskId());
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot claim task" + releaseTaskPayload.getTaskId() + " due it is not a candidate for it");
        }
        // validate the the task doesn't have an assignee
        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            throw new IllegalStateException("You cannot release a task that is not claimed");
        }
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        // validate that you are trying to release task where you are the assignee
        if (!task.getAssignee().equals(authenticatedUserId)) {
            throw new IllegalStateException("You cannot release a task where you are not the assignee");
        }

        taskService.unclaim(releaseTaskPayload.getTaskId());
        return task(releaseTaskPayload.getTaskId());
    }

    @Override
    public Task update(UpdateTaskPayload updateTaskPayload) {
        // Validate that the task is visible by the authenticated user
        Task task;
        try {
            task = task(updateTaskPayload.getTaskId());
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot update the task" + updateTaskPayload.getTaskId() + " due it is not the current assignee");
        }
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        // validate that you are trying to update task where you are the assignee
        if (!Objects.equals(task.getAssignee(), authenticatedUserId)) {
            throw new IllegalStateException("You cannot update a task where you are not the assignee");
        }
        org.activiti.engine.task.Task internalTask = getInternalTask(updateTaskPayload.getTaskId());

        if (updateTaskPayload.getTaskName() != null) {
            internalTask.setName(updateTaskPayload.getTaskName());
        }
        if (updateTaskPayload.getDescription() != null) {
            internalTask.setDescription(updateTaskPayload.getDescription());
        }
        if (updateTaskPayload.getPriority() != null) {
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
        Task task;
        try {
            task = task(deleteTaskPayload.getTaskId());
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot delete the task" + deleteTaskPayload.getTaskId() + " due it is not the current assignee");
        }
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        // validate that you are trying to delete task where you are the assignee or the owner
        if ((task.getAssignee() == null || task.getAssignee().isEmpty() || !task.getAssignee().equals(authenticatedUserId))
                && (task.getOwner() == null || task.getOwner().isEmpty() || !task.getOwner().equals(authenticatedUserId))) {
            throw new IllegalStateException("You cannot delete a task where you are not the assignee/owner");
        }
        TaskImpl deletedTaskData = new TaskImpl(task.getId(),
                                                task.getName(),
                                                Task.TaskStatus.DELETED);
        if (!deleteTaskPayload.hasReason()) {
            deleteTaskPayload.setReason("Cancelled by " + authenticatedUserId);
        }
        taskService.deleteTask(deleteTaskPayload.getTaskId(),
                               deleteTaskPayload.getReason(),
                               true);
        return deletedTaskData;
    }

    @Override
    public Task create(CreateTaskPayload createTaskPayload) {
        org.activiti.engine.task.Task task = taskService.newTask();
        task.setName(createTaskPayload.getName());
        task.setDescription(createTaskPayload.getDescription());
        task.setDueDate(createTaskPayload.getDueDate());
        task.setPriority(createTaskPayload.getPriority());
        if (createTaskPayload.getAssignee() != null && !createTaskPayload.getAssignee().isEmpty()) {
            task.setAssignee(createTaskPayload.getAssignee());
        }
        task.setParentTaskId(createTaskPayload.getParentTaskId());
        task.setOwner(securityManager.getAuthenticatedUserId());
        taskService.saveTask(task);
        taskService.addCandidateUser(task.getId(),
                                     securityManager.getAuthenticatedUserId());
        if (createTaskPayload.getGroups() != null && !createTaskPayload.getGroups().isEmpty()) {
            for (String g : createTaskPayload.getGroups()) {
                taskService.addCandidateGroup(task.getId(),
                                              g);
            }
        }

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
