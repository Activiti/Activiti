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

import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.payloads.ClaimTaskPayload;
import org.activiti.runtime.api.model.payloads.CompleteTaskPayload;
import org.activiti.runtime.api.model.payloads.DeleteTaskPayload;
import org.activiti.runtime.api.model.payloads.GetTasksPayload;
import org.activiti.runtime.api.model.payloads.ReleaseTaskPayload;
import org.activiti.runtime.api.model.payloads.UpdateTaskPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;

/*
 * All the methods require an authenticated Admin user
 */
public interface TaskAdminRuntime {

    /*
     * Deletes a task
     * - no matter the assignee or if the admin user can see the task
     */
    Task delete(DeleteTaskPayload deleteTaskPayload);

    /*
     * Get Task By Id
     */
    Task task(String taskId);

    /*
     * Get all tasks
     */
    Page<Task> tasks(Pageable pageable);

    /*
     * Get all tasks with payload filters
     */
    Page<Task> tasks(Pageable pageable,
                     GetTasksPayload getTasksPayload);

//    /*
//     * Claim a task with impersonation support
//     *  - The user that is clamming the task is supported as parameter
//     *  - after the claim the task should be in assigned status
//     */
//    Task claim(ClaimTaskPayload claimTaskPayload);
//
//    /*
//     * Release a previously claimed task  with impersonation support
//     */
//    Task release(ReleaseTaskPayload releaseTaskPayload);
//
//    /*
//     * Completes the selected task with impersonation support
//     */
//    Task complete(CompleteTaskPayload completeTaskPayload);
//
//
//    /*
//     * Updates details with impersonation support
//     * - The authenticated user should be able to see the task in order to update its details
//     * - The authenticated user needs to be the assignee of the task to update its details, if not he/she will need to claim the task first
//     */
//    Task update(UpdateTaskPayload updateTaskPayload);


}
