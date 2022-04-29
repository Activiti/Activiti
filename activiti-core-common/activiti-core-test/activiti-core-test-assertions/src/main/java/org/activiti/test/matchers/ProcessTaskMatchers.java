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

import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ProcessTaskMatchers {

    private String taskName;

    private ProcessTaskMatchers(String taskName) {
        this.taskName = taskName;
    }

    public static ProcessTaskMatchers taskWithName(String taskName) {
        return new ProcessTaskMatchers(taskName);
    }

    public OperationScopeMatcher hasBeenCreated() {

        return (operationScope, events) -> {
            hasBeenStarted().match(operationScope, events);
            List<TaskCreatedEvent> taskCreatedEvents = events
                    .stream()
                    .filter(event -> TaskRuntimeEvent.TaskEvents.TASK_CREATED.equals(event.getEventType()))
                    .map(TaskCreatedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(taskCreatedEvents)
                    .filteredOn(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .extracting(event -> event.getEntity().getName())
                    .contains(taskName);

        };
    }

    public OperationScopeMatcher hasBeenAssigned() {

        return (operationScope, events) -> {
            List<TaskAssignedEvent> taskAssignedEvents = events
                    .stream()
                    .filter(event -> TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED.equals(event.getEventType()))
                    .map(TaskAssignedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(taskAssignedEvents)
                    .filteredOn(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .extracting(event -> event.getEntity().getName())
                    .contains(taskName);

        };
    }

    private OperationScopeMatcher hasBeenStarted() {

        return (operationScope, events) -> {
            List<BPMNActivityStartedEvent> taskStartedEvents = events
                    .stream()
                    .filter(event -> BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED.equals(event.getEventType()))
                    .map(BPMNActivityStartedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(taskStartedEvents)
                    .filteredOn(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .extracting(event -> event.getEntity().getActivityName(),
                                event -> event.getEntity().getActivityType())
                    .as("Unable to find event " + BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED + " for user task " + taskName)
                    .contains(tuple(taskName,
                                    "userTask"));

        };
    }

}
