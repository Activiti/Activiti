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

package org.activiti.test.matchers;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.ProcessCreatedEvent;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.task.model.Task;
import org.activiti.test.TaskSource;

import static org.activiti.api.process.model.events.ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ProcessInstanceMatchers {

    private ProcessInstanceMatchers() {
    }

    public static ProcessInstanceMatchers processInstance() {
        return new ProcessInstanceMatchers();
    }

    public OperationScopeMatcher hasBeenStarted() {
        return (operationScope, events) -> {
            List<ProcessCreatedEvent> processCreatedEvents = events
                    .stream()
                    .filter(event -> ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED.equals(event.getEventType()))
                    .map(ProcessCreatedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(processCreatedEvents)
                    .extracting(event -> event.getEntity().getId())
                    .as("Unable to find related " + ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED.name() + " event!")
                    .contains(operationScope.getProcessInstanceId());

            List<ProcessStartedEvent> processStartedEvents = events
                    .stream()
                    .filter(event -> ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED.equals(event.getEventType()))
                    .map(ProcessStartedEvent.class::cast)
                    .collect(Collectors.toList());

            assertThat(processStartedEvents)
                    .extracting(event -> event.getEntity().getId())
                    .as("Unable to find related " + PROCESS_STARTED.name() + " event!")
                    .contains(operationScope.getProcessInstanceId());
        };
    }

    public OperationScopeMatcher hasBeenCompleted() {
        return (operationScope, events) -> {
            List<ProcessCompletedEvent> processCompletedEvents = events
                    .stream()
                    .filter(event -> ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED.equals(event.getEventType()))
                    .map(ProcessCompletedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(processCompletedEvents)
                    .extracting(event -> event.getEntity().getId())
                    .as("Unable to find related " + ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED.name() + " event!")
                    .contains(operationScope.getProcessInstanceId());
        };
    }

    public ProcessResultMatcher status(ProcessInstance.ProcessInstanceStatus status){
        return (processInstance) -> assertThat(processInstance.getStatus()).isEqualTo(status);
    }

    public ProcessResultMatcher name(String name) {
        return (processInstance) ->
                assertThat(processInstance.getName()).isEqualTo(name);
    }

    public ProcessResultMatcher businessKey(String businessKey) {
        return (processInstance) ->
                assertThat(processInstance.getBusinessKey()).isEqualTo(businessKey);
    }

    public ProcessTaskMatcher hasTask(String taskName,
                                      Task.TaskStatus taskStatus,
                                      TaskResultMatcher ... taskResultMatchers) {
        return (processInstanceId, taskProviders) -> {
            for (TaskSource provider : taskProviders) {
                if (provider.canHandle(taskStatus)) {
                    List<Task> tasks = provider.getTasks(processInstanceId);
                    assertThat(tasks)
                            .extracting(Task::getName,
                                        Task::getStatus)
                            .contains(tuple(taskName,
                                            taskStatus));
                    Task matchingTask = tasks.stream()
                            .filter(task -> task.getName().equals(taskName))
                            .findFirst()
                            .orElse(null);
                    if (taskResultMatchers != null) {
                        for (TaskResultMatcher taskResultMatcher : taskResultMatchers) {
                            taskResultMatcher.match(matchingTask);
                        }
                    }
                }
            }
        };
    }
}
