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

package org.activiti.runtime.api;

import java.util.List;

import org.activiti.runtime.api.conf.TaskRuntimeConfiguration;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.VariableInstance;
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

    List<VariableInstance> variables(GetTaskVariablesPayload getTaskVariablesPayload);

    void setVariables(SetTaskVariablesPayload setTaskVariablesPayload);
}
