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

package org.activiti.test.config;

import java.util.List;

import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableDeletedEvent;
import org.activiti.api.model.shared.event.VariableUpdatedEvent;
import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.model.events.BPMNErrorReceivedEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.process.model.events.BPMNTimerCancelledEvent;
import org.activiti.api.process.model.events.BPMNTimerExecutedEvent;
import org.activiti.api.process.model.events.BPMNTimerFailedEvent;
import org.activiti.api.process.model.events.BPMNTimerFiredEvent;
import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.ProcessCreatedEvent;
import org.activiti.api.process.runtime.events.ProcessResumedEvent;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.process.runtime.events.ProcessSuspendedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.api.task.runtime.events.TaskSuspendedEvent;
import org.activiti.api.task.runtime.events.TaskUpdatedEvent;
import org.activiti.api.task.runtime.events.listener.TaskEventListener;
import org.activiti.test.EventSource;
import org.activiti.test.LocalEventSource;
import org.activiti.test.LocalTaskSource;
import org.activiti.test.TaskSource;
import org.activiti.test.operations.ProcessRuntimeOperations;
import org.activiti.test.operations.TaskRuntimeOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiAssertionsAutoConfiguration {

    private final LocalEventSource localEventProvider = new LocalEventSource();

    @Bean
    public LocalEventSource handledEvents() {
        return localEventProvider;
    }

    @Bean
    public TaskSource localTaskProvider(TaskRuntime taskRuntime) {
        return new LocalTaskSource(taskRuntime);
    }

    @Bean
    public ProcessRuntimeOperations processRuntimeOperations(ProcessRuntime processRuntime,
                                                             EventSource eventSource,
                                                             List<TaskSource> taskSources) {
        return new ProcessRuntimeOperations(processRuntime,
                                            eventSource,
                                            taskSources);
    }

    @Bean
    public TaskRuntimeOperations taskRuntimeOperations(TaskRuntime taskRuntime,
                                                       EventSource eventSource,
                                                       List<TaskSource> taskSources) {
        return new TaskRuntimeOperations(taskRuntime,
                                         eventSource,
                                         taskSources);
    }

    @Bean
    public BPMNElementEventListener<BPMNActivityStartedEvent> keepInMemoryBpmnActivityStartedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public BPMNElementEventListener<BPMNActivityCompletedEvent> keepInMemoryBpmnActivityCompletedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public BPMNElementEventListener<BPMNActivityCancelledEvent> keepInMemoryBpmnActivityCancelledListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public BPMNElementEventListener<BPMNSequenceFlowTakenEvent> keepInMemoryBpmnSequenceFlowTakenListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCreatedEvent> keepInMemoryProcessCreatedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessStartedEvent> keepInMemoryProcessStartedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCompletedEvent> keepInMemoryProcessCompletedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessResumedEvent> keepInMemoryProcessResumedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessSuspendedEvent> keepInMemoryProcessSuspendedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCancelledEvent> keepInMemoryProcessCancelledListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public VariableEventListener<VariableCreatedEvent> keepInMemoryVariableCreatedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public VariableEventListener<VariableDeletedEvent> keepInMemoryVariableDeletedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public VariableEventListener<VariableUpdatedEvent> keepInMemoryVariableUpdatedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public TaskEventListener<TaskCreatedEvent> keepInMemoryTaskCreatedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public TaskEventListener<TaskUpdatedEvent> keepInMemoryTaskUpdatedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public TaskEventListener<TaskCompletedEvent> keepInMemoryTaskCompletedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public TaskEventListener<TaskSuspendedEvent> keepInMemoryTaskSuspendedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public TaskEventListener<TaskAssignedEvent> keepInMemoryTaskAssignedEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public TaskEventListener<TaskCancelledEvent> keepInMemoryTaskCancelledEventListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public BPMNElementEventListener<BPMNSignalReceivedEvent> keepInMemoryBpmnSignalReceivedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public  BPMNElementEventListener<BPMNTimerScheduledEvent> keepInMemoryTimerScheduledListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public  BPMNElementEventListener<BPMNTimerFiredEvent> keepInMemoryTimerFiredListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public  BPMNElementEventListener<BPMNTimerExecutedEvent> keepInMemoryTimerExecutedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public  BPMNElementEventListener<BPMNTimerFailedEvent> keepInMemoryTimerFailedListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public  BPMNElementEventListener<BPMNTimerCancelledEvent> keepInMemoryTimerCancelledListener() {
        return localEventProvider::addCollectedEvents;
    }

    @Bean
    public  BPMNElementEventListener<BPMNErrorReceivedEvent> keepInMemoryErrorReceivedListener() {
        return localEventProvider::addCollectedEvents;
    }


}
