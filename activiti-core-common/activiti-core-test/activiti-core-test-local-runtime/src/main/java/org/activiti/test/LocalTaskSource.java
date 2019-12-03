/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.test;

import java.util.List;

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;

public class LocalTaskSource implements TaskSource {

    private static final int MAX_ITEMS = 1000;
    private TaskRuntime taskRuntime;

    public LocalTaskSource(TaskRuntime taskRuntime) {
        this.taskRuntime = taskRuntime;
    }

    @Override
    public List<Task> getTasks(String processInstanceId) {
        Page<Task> taskPage = taskRuntime.tasks(Pageable.of(0,
                                                         MAX_ITEMS),
                                             TaskPayloadBuilder.tasks().withProcessInstanceId(processInstanceId).build());
        return taskPage.getContent();
    }

    @Override
    public boolean canHandle(Task.TaskStatus taskStatus) {
        switch (taskStatus) {
            case CREATED:
            case ASSIGNED:
            case SUSPENDED:
                return true;
        }
        return false;
    }
}
