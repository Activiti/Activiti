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
package org.activiti.test.matchers;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskMatchers {

    private TaskMatchers() {
    }

    public static TaskMatchers task() {
        return new TaskMatchers();
    }

    public OperationScopeMatcher hasBeenAssigned() {
        return (operationScope, events) -> {
            List<TaskAssignedEvent> taskAssignedEvents = events
                    .stream()
                    .filter(event -> TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED.equals(event.getEventType()))
                    .map(TaskAssignedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(taskAssignedEvents)
                    .extracting(event -> event.getEntity().getId())
                    .as("Unable to find event " + TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED + " for task " + operationScope.getTaskId())
                    .contains(operationScope.getTaskId());
        };
    }

    public OperationScopeMatcher hasBeenCompleted() {
        return (operationScope, events) -> {
            List<TaskCompletedEvent> taskCompletedEvents = events
                    .stream()
                    .filter(event -> TaskRuntimeEvent.TaskEvents.TASK_COMPLETED.equals(event.getEventType()))
                    .map(TaskCompletedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(taskCompletedEvents)
                    .extracting(event -> event.getEntity().getId())
                    .as("Unable to find event " + TaskRuntimeEvent.TaskEvents.TASK_COMPLETED + " for task " + operationScope.getTaskId())
                    .contains(operationScope.getTaskId());
        };
    }

    public TaskResultMatcher assignee(String assignee) {
        return withAssignee(assignee);
    }

    public static TaskResultMatcher withAssignee(String assignee) {
        return task -> assertThat(task.getAssignee()).isEqualTo(assignee);
    }
}
