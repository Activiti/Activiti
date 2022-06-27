/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.api.task.model.payloads.CreateTaskPayload;
import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;
import org.activiti.api.task.model.payloads.DeleteTaskPayload;
import org.activiti.api.task.model.payloads.GetTaskVariablesPayload;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.model.payloads.ReleaseTaskPayload;
import org.activiti.api.task.model.payloads.SaveTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;
import org.activiti.api.task.runtime.conf.TaskRuntimeConfiguration;


/**
 * User Based Integrations against the Task Runtime
 */
public interface TaskRuntime {

    TaskRuntimeConfiguration configuration();

    /**
     * Get task by id if the authenticated user:
     *  - is the assignee or
     *  - is in a group with is assigned to the task or
     *  - has admin role
     */
    Task task(String taskId);

    /**
     * Get all tasks where
     *  - the authenticated user is the actual assignee
     *  - the user belongs to a group that is a candidate for the task
     */
    Page<Task> tasks(Pageable pageable);

    /**
     * Get all tasks where applying the filters in the Payload
     *  - the authenticated user is the actual assignee
     *  - the user belongs to a group that is a candidate for the task
     */
    Page<Task> tasks(Pageable pageable,
                     GetTasksPayload getTasksPayload);

    /**
     * Creates a task based on the following rules
     *  - If an assignee is provided it creates and assign the task to the provided user
     *  - If there is no assignee the task is not assigned, just created
     *  - The owner of the task is the currently authenticated user (which is automatically added as a candidate)
     *  - If a group or list of groups is provided those groups are added as candidates for claiming the task
     */
    Task create(CreateTaskPayload createTaskPayload);

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
     * Saves the selected task with the variables set in the payload in the task scope
     * - This method checks that the task is visible by the authenticated user
     * - This method also check that the task is assigned to the currently authenticated user
     */
    void save(SaveTaskPayload saveTaskPayload);

    /**
     * Updates details of a task
     * - The authenticated user should be able to see the task in order to update its details
     * - The authenticated user needs to be the assignee of the task to update its details, if not he/she will need to claim the task first
     */
    Task update(UpdateTaskPayload updateTaskPayload);

    /**
     * Deletes a task
     * - The authenticated user should be able to see the task in order to delete it
     * - The authenticated user needs to be the assignee of the task in order to delete it
     * - this method returns a shallow Task with the necessary information to validate that the task was deleted
     */
    Task delete(DeleteTaskPayload deleteTaskPayload);

    void createVariable(CreateTaskVariablePayload createTaskVariablePayload);
    void updateVariable(UpdateTaskVariablePayload updateTaskVariablePayload);
    List<VariableInstance> variables(GetTaskVariablesPayload getTaskVariablesPayload);

    void addCandidateUsers(CandidateUsersPayload candidateUsersPayload);
    void deleteCandidateUsers(CandidateUsersPayload candidateUsersPayload);

    void addCandidateGroups(CandidateGroupsPayload candidateGroupsPayload);
    void deleteCandidateGroups(CandidateGroupsPayload candidateGroupsPayload);

    List<String> userCandidates(String taskId);
    List<String> groupCandidates(String taskId);

    /**
     * Assign a task that has been claimed before to a different user
     * - Only the current assignee can perform the action
     * - The new assignee should be part of the candidate users for this task
     */
    Task assign(AssignTaskPayload assignTaskPayload);
}
