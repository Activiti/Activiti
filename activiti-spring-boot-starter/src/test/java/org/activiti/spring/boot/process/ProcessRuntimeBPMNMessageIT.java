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

import java.util.LinkedList;
import java.util.List;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
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
public class ProcessRuntimeBPMNMessageIT {

    private static final String PROCESS_INTERMEDIATE_THROW_MESSAGE_EVENT = "intermediateThrowMessageEvent";

    @Autowired
    private ProcessRuntime processRuntime;
    
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;
    
    private List<ActivitiEvent> receivedEvents = new LinkedList<>();

    @Before
    public void setUp() {
        receivedEvents.clear();
        
        //given
        runtimeService.addEventListener(new ActivitiEventListener() {  
            
            boolean isEventToAdd(ActivitiEvent event) {
                if (event instanceof ActivitiActivityEvent) return true;     
                return false;
            }
            
            @Override
            public void onEvent(ActivitiEvent event) {
                if (isEventToAdd(event)) {
                    receivedEvents.add(event);   
                }     
            }

            @Override
            public boolean isFailOnException() {
                // TODO Auto-generated method stub
                return false;
            }            
        }); 
    }

    @After
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
        receivedEvents.clear();
    }

    @Test
    public void shoulEventsForProcessWithThrowMessage() throws Exception{

        
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withProcessDefinitionKey(PROCESS_INTERMEDIATE_THROW_MESSAGE_EVENT)
                                                       .build());
        
        assertThat(receivedEvents)
        .filteredOn(event -> event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT))
        .isNotEmpty()
        .extracting(event -> event.getType(),
                    event -> event.getProcessDefinitionId(),
                    event -> event.getProcessInstanceId(),
                    event -> ((ActivitiMessageEvent)event).getActivityType(),
                    event -> ((ActivitiMessageEvent)event).getMessageName())
        .contains(Tuple.tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT,
                              process.getProcessDefinitionId(),
                              process.getId(),
                              "throwEvent",
                              "Test Message"));

    }
}
