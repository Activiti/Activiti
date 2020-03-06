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

package org.activiti.api.task.runtime;

import java.util.List;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.payloads.AssignTaskPayload;
import org.activiti.api.task.model.payloads.CandidateGroupsPayload;
import org.activiti.api.task.model.payloads.CandidateUsersPayload;
import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;
import org.activiti.api.task.model.payloads.DeleteTaskPayload;
import org.activiti.api.task.model.payloads.GetTaskVariablesPayload;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.model.payloads.ReleaseTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;

/**
 * All the methods require an authenticated Admin user
 */
public interface TaskAdminRuntime {

    /**
     * Deletes a task
     * - no matter the assignee or if the admin user can see the task
     */
    Task delete(DeleteTaskPayload deleteTaskPayload);

    /**
     * Get Task By Id
     */
    Task task(String taskId);

    /**
     * Get all tasks
     */
    Page<Task> tasks(Pageable pageable);

    /**
     * Get all tasks with payload filters
     */
    Page<Task> tasks(Pageable pageable,
                     GetTasksPayload getTasksPayload);

    /**
     * Claim a task with the currently authenticated user
     *  - If there is no authenticated user throw an IllegalStateException
     *  - If the currently authenticated user is not a candidate throw an IllegalStateException
     *  - The current approach doesn't support impersonation, it will always take the currently authenticated user
     *  - after the claim the task should be in assigned status
     */
    Task claim(ClaimTaskPayload claimTaskPayload);

    /**
     * Release a previously claimed task
     * - The authenticated user needs to be the assignee in order to release it
     */
    Task release(ReleaseTaskPayload releaseTaskPayload);

    /**
     * Completes the selected task with the variables set in the payload
     * - This method checks that the task is visible by the authenticated user
     * - This method also check that the task is assigned to the currently authenticated user before complete
     * - This method return a shallow Task object with the basic information needed to validate that the task was completed
     */
    Task complete(CompleteTaskPayload completeTaskPayload);
    
    /**
     * Updates details of a task
     */
    Task update(UpdateTaskPayload updateTaskPayload);

    void createVariable(CreateTaskVariablePayload createTaskVariablePayload);
    void updateVariable(UpdateTaskVariablePayload updateTaskVariablePayload);
    List<VariableInstance> variables(GetTaskVariablesPayload getTaskVariablesPayload);

    /**
     * Assign a task with a new user
     *  - If there is a user assigned, reassign it to the new user
     *  - after the assign the task should be in assigned status
     */
    Task assign(AssignTaskPayload assignTaskPayload);
    
    void addCandidateUsers(CandidateUsersPayload candidateUsersPayload);
    void deleteCandidateUsers(CandidateUsersPayload candidateUsersPayload);
    
    void addCandidateGroups(CandidateGroupsPayload candidateGroupsPayload);
    void deleteCandidateGroups(CandidateGroupsPayload candidateGroupsPayload);
    
    List<String> userCandidates(String taskId);
    List<String> groupCandidates(String taskId);
    
}
