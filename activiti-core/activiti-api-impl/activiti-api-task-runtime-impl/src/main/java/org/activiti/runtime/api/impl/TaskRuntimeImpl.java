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

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.model.payloads.CandidateGroupsPayload;
import org.activiti.api.task.model.payloads.CandidateUsersPayload;
import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.model.payloads.CreateTaskPayload;
import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;
import org.activiti.api.task.model.payloads.DeleteTaskPayload;
import org.activiti.api.task.model.payloads.GetTaskVariablesPayload;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.model.payloads.ReleaseTaskPayload;
import org.activiti.api.task.model.payloads.SaveTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.api.task.runtime.conf.TaskRuntimeConfiguration;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@PreAuthorize("hasRole('ACTIVITI_USER')")
public class TaskRuntimeImpl implements TaskRuntime {

    private final TaskService taskService;

    private final APITaskConverter taskConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final TaskRuntimeConfiguration configuration;

    private final SecurityManager securityManager;

    private final TaskRuntimeHelper taskRuntimeHelper;

    public TaskRuntimeImpl(TaskService taskService,
                           SecurityManager securityManager,
                           APITaskConverter taskConverter,
                           APIVariableInstanceConverter variableInstanceConverter,
                           TaskRuntimeConfiguration configuration,
                           TaskRuntimeHelper taskRuntimeHelper) {
        this.taskService = taskService;
        this.securityManager = securityManager;
        this.taskConverter = taskConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.configuration = configuration;
        this.taskRuntimeHelper = taskRuntimeHelper;
    }

    @Override
    public TaskRuntimeConfiguration configuration() {
        return configuration;
    }

    @Override
    public Task task(String taskId) {
        return taskConverter.fromWithCandidates(taskRuntimeHelper.getInternalTaskWithChecks(taskId));
    }

    @Override
    public Page<Task> tasks(Pageable pageable) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        if (authenticatedUserId != null && !authenticatedUserId.isEmpty()) {
            List<String> userGroups = securityManager.getAuthenticatedUserGroups();
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
            List<String> userGroups = securityManager.getAuthenticatedUserGroups();
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
        
        taskRuntimeHelper.handleCompleteTaskPayload(completeTaskPayload);
                
        taskService.complete(completeTaskPayload.getTaskId(),
                completeTaskPayload.getVariables(), true);


        ((TaskImpl) task).setStatus(Task.TaskStatus.COMPLETED);

        return task;
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
            throw new IllegalStateException("The authenticated user cannot release task" + releaseTaskPayload.getTaskId() + " due it is not a candidate for it");
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
        return taskRuntimeHelper.applyUpdateTaskPayload(false, updateTaskPayload);
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
        if ((task.getAssignee() == null || task.getAssignee().isEmpty() || !task.getAssignee().equals(authenticatedUserId)) &&
                (task.getOwner() == null || task.getOwner().isEmpty() || !task.getOwner().equals(authenticatedUserId))) {
            throw new IllegalStateException("You cannot delete a task where you are not the assignee/owner");
        }
        TaskImpl deletedTaskData = new TaskImpl(task.getId(),
                task.getName(),
                Task.TaskStatus.CANCELLED);
        if (!deleteTaskPayload.hasReason()) {
            deleteTaskPayload.setReason("Task deleted by " + authenticatedUserId);
        }
        taskService.deleteTask(deleteTaskPayload.getTaskId(),
                deleteTaskPayload.getReason(),
                true);
        return deletedTaskData;
    }

    @Override
    public Task create(CreateTaskPayload createTaskPayload) {
        if (createTaskPayload.getName() == null || createTaskPayload.getName().isEmpty()) {
            throw new IllegalStateException("You cannot create a task without name");
        }

        org.activiti.engine.task.Task task = taskService.newTask();
        task.setName(createTaskPayload.getName());
        task.setDescription(createTaskPayload.getDescription());
        task.setDueDate(createTaskPayload.getDueDate());
        task.setPriority(createTaskPayload.getPriority());
        if (createTaskPayload.getAssignee() != null && !createTaskPayload.getAssignee().isEmpty()) {
            task.setAssignee(createTaskPayload.getAssignee());
        }
        task.setParentTaskId(createTaskPayload.getParentTaskId());
        task.setFormKey(createTaskPayload.getFormKey());
        task.setOwner(securityManager.getAuthenticatedUserId());
        taskService.saveTask(task);
        if (createTaskPayload.getCandidateGroups() != null && !createTaskPayload.getCandidateGroups().isEmpty()) {
            for ( String g : createTaskPayload.getCandidateGroups() ) {
                taskService.addCandidateGroup(task.getId(),
                        g);
            }
        }

        if (createTaskPayload.getCandidateUsers() != null && !createTaskPayload.getCandidateUsers().isEmpty()) {
            for ( String u : createTaskPayload.getCandidateUsers() ) {
                taskService.addCandidateUser(task.getId(),
                        u);
            }
        }

        return taskConverter.from(task);
    }

    @Override
    public void addCandidateUsers(CandidateUsersPayload candidateUsersPayload) {
        org.activiti.engine.task.Task internalTask;
        try {
            internalTask = taskRuntimeHelper.getInternalTaskWithChecks(candidateUsersPayload.getTaskId());

        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot update the task" + candidateUsersPayload.getTaskId() + " due it is not the current assignee");
        }

        String authenticatedUserId = securityManager.getAuthenticatedUserId();

        // validate that you are trying to add CandidateUsers to the task where you are the assignee
        if (!Objects.equals(internalTask.getAssignee(), authenticatedUserId)) {
            throw new IllegalStateException("You cannot update a task where you are not the assignee");
        }


        if (candidateUsersPayload.getCandidateUsers() != null && !candidateUsersPayload.getCandidateUsers().isEmpty()) {
            for ( String u : candidateUsersPayload.getCandidateUsers() ) {
                taskService.addCandidateUser(internalTask.getId(),
                        u);
            }
        }
    }

    @Override
    public void deleteCandidateUsers(CandidateUsersPayload candidateUsersPayload) {
        org.activiti.engine.task.Task internalTask;
        try {
            internalTask = taskRuntimeHelper.getInternalTaskWithChecks(candidateUsersPayload.getTaskId());

        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot update the task" + candidateUsersPayload.getTaskId() + " due it is not the current assignee");
        }

        String authenticatedUserId = securityManager.getAuthenticatedUserId();

        // validate that you are trying to add CandidateUsers to the task where you are the assignee
        if (!Objects.equals(internalTask.getAssignee(), authenticatedUserId)) {
            throw new IllegalStateException("You cannot update a task where you are not the assignee");
        }


        if (candidateUsersPayload.getCandidateUsers() != null && !candidateUsersPayload.getCandidateUsers().isEmpty()) {
            for ( String u : candidateUsersPayload.getCandidateUsers() ) {
                taskService.deleteCandidateUser(internalTask.getId(),
                        u);
            }
        }
    }

    @Override
    public void addCandidateGroups(CandidateGroupsPayload candidateGroupsPayload) {
        org.activiti.engine.task.Task internalTask;
        try {
            internalTask = taskRuntimeHelper.getInternalTaskWithChecks(candidateGroupsPayload.getTaskId());

        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot update the task" + candidateGroupsPayload.getTaskId() + " due it is not the current assignee");
        }

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        // validate that you are trying to add CandidateGroups to the task where you are the assignee
        if (!Objects.equals(internalTask.getAssignee(), authenticatedUserId)) {
            throw new IllegalStateException("You cannot update a task where you are not the assignee");
        }


        if (candidateGroupsPayload.getCandidateGroups() != null && !candidateGroupsPayload.getCandidateGroups().isEmpty()) {
            for ( String g : candidateGroupsPayload.getCandidateGroups() ) {
                taskService.addCandidateGroup(internalTask.getId(),
                        g);
            }
        }
    }

    @Override
    public void deleteCandidateGroups(CandidateGroupsPayload candidateGroupsPayload) {
        org.activiti.engine.task.Task internalTask;
        try {
            internalTask = taskRuntimeHelper.getInternalTaskWithChecks(candidateGroupsPayload.getTaskId());

        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The authenticated user cannot update the task" + candidateGroupsPayload.getTaskId() + " due it is not the current assignee");
        }

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        // validate that you are trying to add CandidateGroups to the task where you are the assignee
        if (!Objects.equals(internalTask.getAssignee(), authenticatedUserId)) {
            throw new IllegalStateException("You cannot update a task where you are not the assignee");
        }


        if (candidateGroupsPayload.getCandidateGroups() != null && !candidateGroupsPayload.getCandidateGroups().isEmpty()) {
            for ( String g : candidateGroupsPayload.getCandidateGroups() ) {
                taskService.deleteCandidateGroup(internalTask.getId(),
                        g);
            }
        }
    }

    @Override
    public List<String> userCandidates(String taskId) {
        List<IdentityLink> identityLinks = getIdentityLinks(taskId);
        List<String> userCandidates = new ArrayList<>();
        if (identityLinks != null) {
            for ( IdentityLink i : identityLinks ) {
                if (i.getUserId() != null) {
                    if (i.getType().equals(IdentityLinkType.CANDIDATE)) {
                        userCandidates.add(i.getUserId());
                    }
                }
            }

        }
        return userCandidates;
    }

    @Override
    public List<String> groupCandidates(String taskId) {
        List<IdentityLink> identityLinks = getIdentityLinks(taskId);
        List<String> groupCandidates = new ArrayList<>();
        if (identityLinks != null) {
            for ( IdentityLink i : identityLinks ) {
                if (i.getGroupId() != null) {
                    if (i.getType().equals(IdentityLinkType.CANDIDATE)) {
                        groupCandidates.add(i.getGroupId());
                    }
                }
            }

        }
        return groupCandidates;
    }

    @Override
    public List<VariableInstance> variables(GetTaskVariablesPayload getTaskVariablesPayload) {
        taskRuntimeHelper.assertHasAccessToTask(getTaskVariablesPayload.getTaskId());
        return variableInstanceConverter.from(taskRuntimeHelper.getInternalTaskVariables(getTaskVariablesPayload.getTaskId()).values());
    }

    @Override
    public void createVariable(CreateTaskVariablePayload createTaskVariablePayload) {
        taskRuntimeHelper.createVariable(false, createTaskVariablePayload);
    }

    @Override
    public void updateVariable(UpdateTaskVariablePayload updateTaskVariablePayload) {
        taskRuntimeHelper.updateVariable(false, updateTaskVariablePayload);
    }

    @Override
    public void save(SaveTaskPayload saveTaskPayload) {
        taskRuntimeHelper.assertHasAccessToTask(saveTaskPayload.getTaskId());

        taskRuntimeHelper.handleSaveTaskPayload(saveTaskPayload);
        
        taskService.setVariablesLocal(saveTaskPayload.getTaskId(),
                saveTaskPayload.getVariables());
    }


    private List<IdentityLink> getIdentityLinks(String taskId) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        if (authenticatedUserId != null && !authenticatedUserId.isEmpty()) {
            List<String> userRoles = securityManager.getAuthenticatedUserRoles();
            List<String> userGroups = securityManager.getAuthenticatedUserGroups();
            org.activiti.engine.task.Task internalTask = taskService.createTaskQuery().taskCandidateOrAssigned(authenticatedUserId,
                    userGroups).taskId(taskId).singleResult();
            if (internalTask == null) {
                throw new NotFoundException("Unable to find task for the given id: " + taskId + " for user: " + authenticatedUserId + " (with groups: " + userGroups + " & with roles: " + userRoles + ")");
            }
            return taskService.getIdentityLinksForTask(taskId);
        }
        throw new IllegalStateException("There is no authenticated user, we need a user authenticated to find tasks");
    }

}
