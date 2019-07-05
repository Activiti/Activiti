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

import java.util.Date;
import java.util.List;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNTimerCancelledEvent;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.BPMNTimerFiredEvent;
import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.test.JobTestHelper;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerCancelledListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerExecutedListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerFiredListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerScheduledListener;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeBPMNTimerIT {
    
    private static final String PROCESS_INTERMEDIATE_TIMER_EVENT = "intermediateTimerEventExample";
    private static final String PROCESS_TIMER_CANCELLED_EVENT = "testTimerCancelledEvent";
 
    @Autowired
    private ProcessRuntime processRuntime;
    
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
    private  DummyBPMNTimerExecutedListener listenerExecuted;
    
    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;
    
    @Autowired
    private ManagementService managementService;
    
    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    
    @Before
    public void setUp() {
        clear();
        
    }

    @After
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void shouldGetTimerFiredScheduledEventsForProcessWithTimer() {

        //given
        securityUtil.logInAs("salaboy");
           
        //when
        Date startTime = new Date();
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(PROCESS_INTERMEDIATE_TIMER_EVENT)
                .build());
        
        List<BPMNTimerScheduledEvent> eventsScheduled = listenerScheduled.getEvents();
        assertThat(eventsScheduled)
            .extracting(BPMNTimerEvent::getEventType,
                        BPMNTimerEvent::getProcessDefinitionId,
                        event -> event.getEntity().getProcessDefinitionId(),
                        event -> event.getEntity().getProcessInstanceId(),
                        event -> event.getEntity().getElementId(),
                        event -> event.getEntity().getTimerPayload().getActivityId()         
            )
            .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_SCHEDULED,
                                  process.getProcessDefinitionId(),
                                  process.getProcessDefinitionId(),
                                  process.getId(),
                                  "timer",
                                  "timer"
                                  )
             
        );
        List<BPMNTimerFiredEvent> eventsFired = listenerFired.getEvents();
        assertThat(eventsFired.size()).isEqualTo(0);
        
        
        long waitTime = 50 * 60 * 1000;
        Date dueDate = new Date(startTime.getTime() + waitTime);
        
        // After setting the clock to time '50minutes and 5 seconds', the second timer should fire
        processEngineConfiguration.getClock().setCurrentTime(new Date(dueDate.getTime() + 5000));
        waitForJobExecutorToProcessAllJobs(5000L, 25L);
        
        
        //then
        eventsFired = listenerFired.getEvents();
         
        assertThat(eventsFired)
                .extracting(BPMNTimerEvent::getEventType,
                            BPMNTimerEvent::getProcessDefinitionId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getElementId(),
                            event -> event.getEntity().getTimerPayload().getActivityId()         
                )
                .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_FIRED,
                                      process.getProcessDefinitionId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "timer",
                                      "timer"
                                      )
                     
                );
        
        assertThat(listenerExecuted.getEvents())
                .extracting(BPMNTimerEvent::getEventType,
                            BPMNTimerEvent::getProcessDefinitionId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getElementId(),
                            event -> event.getEntity().getTimerPayload().getActivityId()          
                )
                .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_EXECUTED,
                                      process.getProcessDefinitionId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "timer",
                                      "timer"
                                      )
             
        );
 
        
    }
    
    @Test
    public void shouldGetTimerCanceledEventByManagementService() throws Exception {
        // GIVEN
        securityUtil.logInAs("salaboy");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withProcessDefinitionKey(PROCESS_TIMER_CANCELLED_EVENT)
                                                       .build());
        
        String jobId = managementService.createTimerJobQuery().singleResult().getId();

        assertThat(jobId).isNotNull();
        
        // WHEN
        managementService.deleteTimerJob(jobId);

        // THEN
        List<BPMNTimerCancelledEvent> eventsCancelled = listenerCancelled.getEvents();
        assertThat(eventsCancelled)
            .extracting(BPMNTimerEvent::getEventType,
                        BPMNTimerEvent::getProcessDefinitionId,
                        event -> event.getEntity().getProcessDefinitionId(),
                        event -> event.getEntity().getProcessInstanceId(),
                        event -> event.getEntity().getElementId(),
                        event -> event.getEntity().getTimerPayload().getActivityId()         
            )
            .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_CANCELLED,
                                  process.getProcessDefinitionId(),
                                  process.getProcessDefinitionId(),
                                  process.getId(),
                                  "timer",
                                  "timer"
                                  )
             
        );
        
        processRuntime.delete(ProcessPayloadBuilder.delete(process.getId()));
    }
    
    @Test
    public void shouldGetTimerCanceledEventByProcessDelete() throws Exception {
        // GIVEN
        securityUtil.logInAs("salaboy");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withProcessDefinitionKey(PROCESS_TIMER_CANCELLED_EVENT)
                                                       .build());
        
        String jobId = managementService.createTimerJobQuery().singleResult().getId();

        assertThat(jobId).isNotNull();
        
        // WHEN
        processRuntime.delete(ProcessPayloadBuilder.delete(process.getId()));

        // THEN
        List<BPMNTimerCancelledEvent> eventsCancelled = listenerCancelled.getEvents();
        assertThat(eventsCancelled)
            .extracting(BPMNTimerEvent::getEventType,
                        BPMNTimerEvent::getProcessDefinitionId,
                        event -> event.getEntity().getProcessDefinitionId(),
                        event -> event.getEntity().getProcessInstanceId(),
                        event -> event.getEntity().getElementId(),
                        event -> event.getEntity().getTimerPayload().getActivityId()         
            )
            .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_CANCELLED,
                                  process.getProcessDefinitionId(),
                                  process.getProcessDefinitionId(),
                                  process.getId(),
                                  "timer",
                                  "timer"
                                  )
             
        );
        
        
    }
    
     
    @Test
    public void shouldGetTimerCanceledEventOnBoundaryEvent() throws Exception {
        //given
        securityUtil.logInAs("salaboy");
        
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(PROCESS_TIMER_CANCELLED_EVENT)
                .build());
        
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
        
        List<BPMNTimerScheduledEvent> eventsScheduled = listenerScheduled.getEvents();
        assertThat(eventsScheduled)
            .extracting(BPMNTimerEvent::getEventType,
                        BPMNTimerEvent::getProcessDefinitionId,
                        event -> event.getEntity().getProcessDefinitionId(),
                        event -> event.getEntity().getProcessInstanceId(),
                        event -> event.getEntity().getElementId(),
                        event -> event.getEntity().getTimerPayload().getActivityId()          
            )
            .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_SCHEDULED,
                                  process.getProcessDefinitionId(),
                                  process.getProcessDefinitionId(),
                                  process.getId(),
                                  "timer",
                                  "timer"
                                  )
             
        );
      
        clear();
        
        
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10),
                                             TaskPayloadBuilder
                                             .tasks()
                                             .withProcessInstanceId(process.getId())
                                             .build());
        assertThat(tasks.getContent().size()).isEqualTo(1);
        
        
        Task task = tasks.getContent().get(0);
        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());

        List<BPMNTimerCancelledEvent> eventsCanceled = listenerCancelled.getEvents();
        assertThat(eventsCanceled)
            .extracting(BPMNTimerEvent::getEventType,
                        BPMNTimerEvent::getProcessDefinitionId,
                        event -> event.getEntity().getProcessDefinitionId(),
                        event -> event.getEntity().getProcessInstanceId(),
                        event -> event.getEntity().getElementId(),
                        event -> event.getEntity().getTimerPayload().getActivityId()        
            )
            .contains(Tuple.tuple(BPMNTimerEvent.TimerEvents.TIMER_CANCELLED,
                                  process.getProcessDefinitionId(),
                                  process.getProcessDefinitionId(),
                                  process.getId(),
                                  "timer",
                                  "timer"
                                  )
             
        );
        
        
    }
        
    public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
        JobTestHelper.waitForJobExecutorToProcessAllJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }

    public void clear() {
        listenerFired.clear();
        listenerScheduled.clear();
        listenerCancelled.clear();
        listenerExecuted.clear();
    }

}
