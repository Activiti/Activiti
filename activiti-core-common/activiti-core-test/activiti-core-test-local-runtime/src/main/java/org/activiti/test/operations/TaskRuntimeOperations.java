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
package org.activiti.test.operations;

import java.util.List;

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.test.EventSource;
import org.activiti.test.TaskSource;
import org.activiti.test.assertions.TaskAssertions;
import org.activiti.test.assertions.TaskAssertionsImpl;

public class TaskRuntimeOperations implements TaskOperations {

    private TaskRuntime taskRuntime;

    private EventSource eventSource;

    private List<TaskSource> taskSources;

    public TaskRuntimeOperations(TaskRuntime taskRuntime,
                                 EventSource eventSource,
                                 List<TaskSource> taskSources) {
        this.taskRuntime = taskRuntime;
        this.eventSource = eventSource;
        this.taskSources = taskSources;
    }

    @Override
    public TaskAssertions claim(ClaimTaskPayload claimTaskPayload) {
        Task task = taskRuntime.claim(claimTaskPayload);
        return buildTaskAssertions(task);
    }

    private TaskAssertions buildTaskAssertions(Task task) {
        return new TaskAssertionsImpl(task,
                                      taskSources,
                                      eventSource);
    }

    @Override
    public TaskAssertions complete(CompleteTaskPayload completeTaskPayload) {
        Task task = taskRuntime.task(completeTaskPayload.getTaskId());
        taskRuntime.complete(completeTaskPayload);
        return buildTaskAssertions(task);
    }
}
