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

package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.events.BPMNTimerCancelledEvent;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerCancelledListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerExecutedListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerFiredListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerScheduledListener;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(ProcessRuntimeBPMNTimerIT.PROCESS_RUNTIME_BPMN_TIMER_IT)
@Import({TimerTestConfigurator.class,
         DummyBPMNTimerFiredListener.class,
         DummyBPMNTimerScheduledListener.class,
         DummyBPMNTimerCancelledListener.class,
         DummyBPMNTimerExecutedListener.class})
public class ProcessRuntimeBPMNTimerIT {

    private static final String PROCESS_INTERMEDIATE_TIMER_EVENT = "intermediateTimerEventExample";
    private static final String PROCESS_TIMER_CANCELLED_EVENT = "testTimerCancelledEvent";
    public static final String PROCESS_RUNTIME_BPMN_TIMER_IT = "ProcessRuntimeBPMNTimerIT";
    private static final String VARIABLE_MAPPING_PROCESS_START_TIME = "testTimerStartEvent";

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private DummyBPMNTimerFiredListener listenerFired;

    @Autowired
    private DummyBPMNTimerScheduledListener listenerScheduled;

    @Autowired
    private DummyBPMNTimerCancelledListener listenerCancelled;

    @Autowired
    private DummyBPMNTimerExecutedListener listenerExecuted;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    @BeforeEach
    public void setUp() {
        clear();
        processEngineConfiguration.getClock().reset();
    }

    @AfterEach
    public void tearDown() {
        processEngineConfiguration.getClock().reset();
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void shouldGetTimerCanceledEventByProcessDelete() {
        // GIVEN
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(PROCESS_TIMER_CANCELLED_EVENT);

        // WHEN
        processBaseRuntime.delete(processInstance.getId());

        // THEN
        List<BPMNTimerCancelledEvent> eventsCancelled = listenerCancelled.getEvents();
        assertThat(eventsCancelled)
                .extracting(BPMNTimerEvent::getEventType,
                            BPMNTimerEvent::getProcessDefinitionId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getElementId()
                )
                .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_CANCELLED,
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getId(),
                                      "timer"
                          )

                );
    }

    @Test
    public void shouldGetTimerFiredScheduledEventsForProcessWithTimer() throws Exception{
        //given
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(PROCESS_INTERMEDIATE_TIMER_EVENT);

        //when
        List<BPMNTimerScheduledEvent> eventsScheduled = listenerScheduled.getEvents();

        //then
        assertThat(eventsScheduled)
                .extracting(BPMNTimerEvent::getEventType,
                            BPMNTimerEvent::getProcessDefinitionId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getElementId()
                )
                .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_SCHEDULED,
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getId(),
                                      "timer"
                          )

                );
        assertThat(listenerFired.getEvents()).isEmpty();

        //when
        long waitTime = 10 * 60 * 1000;
        Date startTime = new Date();
        Date dueDate = new Date(startTime.getTime() + waitTime);

        // After setting the clock to time '5minutes and 5 seconds', the second timer should fire
        processEngineConfiguration.getClock().setCurrentTime(new Date(dueDate.getTime() + 5000));

        //then
        await().untilAsserted(() -> {
            assertThat(listenerFired.getEvents())
                    .extracting(BPMNTimerEvent::getEventType,
                                BPMNTimerEvent::getProcessDefinitionId,
                                event -> event.getEntity().getProcessDefinitionId(),
                                event -> event.getEntity().getProcessInstanceId(),
                                event -> event.getEntity().getElementId()
                    )
                    .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_FIRED,
                                          processInstance.getProcessDefinitionId(),
                                          processInstance.getProcessDefinitionId(),
                                          processInstance.getId(),
                                          "timer"
                              )

                    );

            assertThat(listenerExecuted.getEvents())
                    .extracting(BPMNTimerEvent::getEventType,
                                BPMNTimerEvent::getProcessDefinitionId,
                                event -> event.getEntity().getProcessDefinitionId(),
                                event -> event.getEntity().getProcessInstanceId(),
                                event -> event.getEntity().getElementId()
                    )
                    .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_EXECUTED,
                                          processInstance.getProcessDefinitionId(),
                                          processInstance.getProcessDefinitionId(),
                                          processInstance.getId(),
                                          "timer"
                              )

                    );
        });

        //then the execution reaches the task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         10),
                                             TaskPayloadBuilder
                                                     .tasks()
                                                     .withProcessInstanceId(processInstance.getId())
                                                     .build());
        assertThat(tasks.getContent()).hasSize(1);
        assertThat(tasks.getContent().get(0).getName()).isEqualTo("User Task");
    }

    @Test
    public void shouldGetTimerCanceledEventOnBoundaryEvent() {
        //given
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(PROCESS_TIMER_CANCELLED_EVENT);

        List<BPMNTimerScheduledEvent> eventsScheduled = listenerScheduled.getEvents();
        assertThat(eventsScheduled)
                .extracting(BPMNTimerEvent::getEventType,
                            BPMNTimerEvent::getProcessDefinitionId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getElementId()
                )
                .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_SCHEDULED,
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getId(),
                                      "timer"
                          )

                );

        clear();

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         10),
                                             TaskPayloadBuilder
                                                     .tasks()
                                                     .withProcessInstanceId(processInstance.getId())
                                                     .build());
        assertThat(tasks.getContent()).hasSize(1);

        Task task = tasks.getContent().get(0);
        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());

        List<BPMNTimerCancelledEvent> eventsCanceled = listenerCancelled.getEvents();
        assertThat(eventsCanceled)
                .extracting(BPMNTimerEvent::getEventType,
                            BPMNTimerEvent::getProcessDefinitionId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getElementId()
                )
                .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_CANCELLED,
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getProcessDefinitionId(),
                                      processInstance.getId(),
                                      "timer"
                          )

                );
    }

    @Test
    public void shouldExecuteProcessWithTimerStartExtension() {

        securityUtil.logInAs("user");

        Date startTime = new Date();

        //when
        Page<ProcessInstance> processInstances = processBaseRuntime.getProcessInstancesPage();

        //then
        assertThat(processInstances).isNotNull();
        assertThat(processInstances.getContent()).isEmpty();

        //when shift 31 minutes
        long waitTime = 31 * 60 * 1000;
        processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + waitTime));

        //then
        await().untilAsserted(() -> {

            securityUtil.logInAs("user");

            Page<ProcessInstance> processInstancePage = processBaseRuntime.getProcessInstancesPage();
            //then
            assertThat(processInstancePage).isNotNull();
            assertThat(processInstancePage.getContent()).isNotEmpty();

            ProcessInstance processInstance = processInstancePage.getContent().get(0);
            assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(VARIABLE_MAPPING_PROCESS_START_TIME);

            List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

            assertThat(variables).extracting(VariableInstance::getName,
                    VariableInstance::getValue)
                    .containsOnly(
                            tuple("process_variable_name",
                                  "value")
                    );
        });
    }

    public void clear() {
        listenerFired.clear();
        listenerScheduled.clear();
        listenerCancelled.clear();
        listenerExecuted.clear();
    }

}
