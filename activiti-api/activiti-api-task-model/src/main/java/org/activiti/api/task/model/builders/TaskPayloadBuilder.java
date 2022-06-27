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
package org.activiti.api.task.model.builders;

import org.activiti.api.process.model.ProcessInstance;

public class TaskPayloadBuilder {

    public static GetTasksPayloadBuilder tasks() {
        return new GetTasksPayloadBuilder();
    }

    public static GetTasksPayloadBuilder tasksForProcess(ProcessInstance processInstance) {
        return new GetTasksPayloadBuilder().withProcessInstanceId(processInstance.getId());
    }

    public static CompleteTaskPayloadBuilder complete() {
        return new CompleteTaskPayloadBuilder();
    }

    public static SaveTaskPayloadBuilder save() {
        return new SaveTaskPayloadBuilder();
    }

    public static ClaimTaskPayloadBuilder claim() {
        return new ClaimTaskPayloadBuilder();
    }

    public static ReleaseTaskPayloadBuilder release() {
        return new ReleaseTaskPayloadBuilder();
    }

    public static CreateTaskVariablePayloadBuilder createVariable() {
        return new CreateTaskVariablePayloadBuilder();
    }

    public static UpdateTaskVariablePayloadBuilder updateVariable() {
        return new UpdateTaskVariablePayloadBuilder();
    }

    public static GetTaskVariablesPayloadBuilder variables() {
        return new GetTaskVariablesPayloadBuilder();
    }

    public static UpdateTaskPayloadBuilder update() {
        return new UpdateTaskPayloadBuilder();
    }

    public static DeleteTaskPayloadBuilder delete() {
        return new DeleteTaskPayloadBuilder();
    }

    public static CreateTaskPayloadBuilder create() {
        return new CreateTaskPayloadBuilder();
    }

    public static AssignTaskPayloadBuilder assign() {
        return new AssignTaskPayloadBuilder();
    }

    public static CandidateUsersPayloadBuilder addCandidateUsers() {
        return new CandidateUsersPayloadBuilder();
    }

    public static CandidateUsersPayloadBuilder deleteCandidateUsers() {
        return new CandidateUsersPayloadBuilder();
    }

    public static CandidateGroupsPayloadBuilder addCandidateGroups() {
        return new CandidateGroupsPayloadBuilder();
    }

    public static CandidateGroupsPayloadBuilder deleteCandidateGroups() {
        return new CandidateGroupsPayloadBuilder();
    }
}
