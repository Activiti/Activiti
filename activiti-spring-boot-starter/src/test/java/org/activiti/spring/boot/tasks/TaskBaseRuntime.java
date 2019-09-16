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

package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.Task.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;

public class TaskBaseRuntime {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private ProcessRuntime processRuntime;

    public List<Task> getTasksByProcessInstanceId(String processInstanceId) {
        List<Task> taskList = taskRuntime.tasks(
                Pageable.of(0,
                        50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(processInstanceId)
                        .build())
                .getContent();
        return taskList;
    }

    public void completeTask(String taskId) {
        this.completeTask(taskId, Collections.<String, Object>emptyMap());
    }

    public void completeTask(String taskId, Map<String, Object> variables) {

        Task completeTask = taskRuntime
                .complete(TaskPayloadBuilder.complete().withTaskId(taskId).withVariables(variables).build());
        assertThat(completeTask).isNotNull();
        assertThat(completeTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    public ProcessInstance startProcessWithProcessDefinitionKey(String processDefinitionKey) {
        return processRuntime.start(ProcessPayloadBuilder.start()
        .withProcessDefinitionKey(processDefinitionKey)
        .build());
    }

}
