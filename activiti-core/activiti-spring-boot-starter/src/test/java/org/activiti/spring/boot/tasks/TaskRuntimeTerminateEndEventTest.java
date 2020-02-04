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
import static org.assertj.core.api.Java6Assertions.tuple;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TaskRuntimeTerminateEndEventTest {

    private static final String TASK_PROCESS_TERMINATE_EVENT = "ProcessTerminateEvent";

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;
    @Autowired
    private ProcessBaseRuntime processBaseRuntime;
    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private LocalEventSource localEventSource;

    @Autowired
    private TaskRuntimeEventListeners taskRuntimeEventListeners;

    @After
    public void tearDown(){
        taskCleanUpUtil.cleanUpWithAdmin();
        localEventSource.clearEvents();
    }

    @Test
    public void should_ProcessesAndTasksDisappear_whenTerminateEventIsExecuted() {
        ProcessInstance process = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_PROCESS_TERMINATE_EVENT);

        List<Task> taskList = taskBaseRuntime.getTasksByProcessInstanceId(process.getId());
        assertThat(taskList).isNotEmpty();
        assertThat(taskList).hasSize(2);

        Task task1 = taskList.get(0);

        taskBaseRuntime.completeTask(task1.getId());

        List<Task> taskAfterCompleted = taskBaseRuntime.getTasksByProcessInstanceId(process.getId());
        assertThat(taskAfterCompleted).hasSize(0);

        List<ProcessInstance> processInstanceList = processBaseRuntime.getProcessInstances();
        assertThat(processInstanceList).hasSize(0);

    }

    @Test
    public void letsDebug(){

        securityUtil.logInAs("user");

        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("Process_KzwZAEl-");
        assertThat(processInstance).isNotNull();

        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks).hasSize(2);
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);
        assertThat(task1.getName()).isEqualTo("task1");
        assertThat(task2.getName()).isEqualTo("task2");

        assertThat(RuntimeTestConfiguration.createdTasks).contains(task1.getId(), task2.getId());

        taskBaseRuntime.completeTask(task2.getId());

        List<Task> tasksAfterCompletion = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasksAfterCompletion).hasSize(0);

        assertThat(RuntimeTestConfiguration.completedTasks).hasSize(1);

        assertThat(taskRuntimeEventListeners.getCancelledTasks()).hasSize(1);

//        assertThat(localEventSource.getTaskEvents())
//            .extracting(event -> ((Task) event.getEntity()).getName(),
//                RuntimeEvent::getEventType
//            )
//            .contains(tuple(task1.getName(),
//                TaskRuntimeEvent.TaskEvents.TASK_CANCELLED));


        List<TaskCancelledEvent> taskCancelledEvents =
            localEventSource.getEvents(TaskCancelledEvent.class);

        assertThat(taskCancelledEvents).hasSize(1);
        assertThat(taskCancelledEvents.get(0).getCause()).contains("Terminated by end event:");


        assertThat(RuntimeTestConfiguration.cancelledProcesses).hasSize(1);

//        assertThat(localEventSource.getProcessInstanceEvents())
////            .extracting(event -> ((ProcessInstance) event.getEntity()).getName(),
////                        RuntimeEvent::getEventType
////            )
//            .filteredOn(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED))
////            .contains(tuple(processInstance.getName(), ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED
////                            ));

        List<RuntimeEvent> processCancelledEvents =
            localEventSource.getProcessInstanceEvents()
            .stream()
            .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED))
            .collect(Collectors.toList());

        assertThat(processCancelledEvents).hasSize(1);
        assertThat(((ProcessCancelledEvent)processCancelledEvents.get(0)).getCause()).contains("Terminated by end event:");

    }

}
