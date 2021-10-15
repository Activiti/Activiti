/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.spring.boot.tasks;

import java.util.ArrayList;
import java.util.List;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRuntimeEventListeners {

    private List<Task> cancelledTasks = new ArrayList<>();

    @Bean
    public TaskRuntimeEventListener<TaskCancelledEvent> taskCancelledListener() {
        return taskCancelledEvent ->
            cancelledTasks.add(taskCancelledEvent.getEntity());
    }

    public List<Task> getCancelledTasks() {
        return cancelledTasks;
    }

    public void clearEvents() {
        cancelledTasks.clear();
    }
}
