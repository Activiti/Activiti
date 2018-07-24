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
import org.activiti.runtime.api.model.payloads.GetSubTasksPayload;
import org.activiti.runtime.api.model.payloads.GetTaskVariablesPayload;
import org.activiti.runtime.api.model.payloads.GetTasksPayload;
import org.activiti.runtime.api.model.payloads.ReleaseTaskPayload;
import org.activiti.runtime.api.model.payloads.SetTaskVariablesPayload;
import org.activiti.runtime.api.model.payloads.UpdateTaskPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;

public interface TaskRuntime {

    TaskRuntimeConfiguration configuration();

    Task task(String taskId);

    Page<Task> tasks(Pageable pageable);

    Page<Task> tasks(Pageable pageable,
                     GetTasksPayload getTasksPayload);

    List<VariableInstance> variables(GetTaskVariablesPayload getTaskVariablesPayload);

    void setVariables(SetTaskVariablesPayload setTaskVariablesPayload);

    Task complete(CompleteTaskPayload completeTaskPayload);

    Task claim(ClaimTaskPayload claimTaskPayload);

    Task release(ReleaseTaskPayload releaseTaskPayload);

    Task update(UpdateTaskPayload updateTaskPayload);

    Task delete(DeleteTaskPayload deleteTaskPayload);

    Task create(CreateTaskPayload createTaskPayload);

    /* this method might be deprecated in future versions of this API */
    List<Task> subTasks(GetSubTasksPayload getSubTasksPayload);

}
