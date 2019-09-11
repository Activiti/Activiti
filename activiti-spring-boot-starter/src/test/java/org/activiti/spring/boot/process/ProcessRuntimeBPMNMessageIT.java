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

import java.util.Collections;
import java.util.List;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNMessageEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.spring.boot.MessageTestConfiguration;
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

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeBPMNMessageIT {

    private static final String EVENT_GATEWAY_MESSAGE = "eventGatewayMessage";

    private static final String SUBPROCESS_MESSAGE = "subprocessMessage";

    private static final String BOUNDARY_MESSAGE = "boundaryMessage";

    private static final String END_MESSAGE = "endMessage";

    private static final String CATCH_MESSAGE = "catchMessage";

    private static final String TEST_MESSAGE = "testMessage";

    private static final String START_MESSAGE_PAYLOAD = "startMessagePayload";

    private static final String PROCESS_INTERMEDIATE_THROW_MESSAGE_EVENT = "intermediateThrowMessageEvent";

    private static final String CATCH_MESSAGE_PAYLOAD = "catchMessagePayload";

    @Autowired
    private ProcessRuntime processRuntime;
    
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;
    
    @Before
    public void setUp() {
        MessageTestConfiguration.messageEvents.clear();
        
    }

    @After
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
        MessageTestConfiguration.messageEvents.clear();
    }

    @Test
    public void shouldThrowIntermediateMessageEvent() {

        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                                            .withBusinessKey("businessKey")
                                                                            .withProcessDefinitionKey(PROCESS_INTERMEDIATE_THROW_MESSAGE_EVENT)
                                                                            .build());
        assertThat(MessageTestConfiguration.messageEvents)
                .isNotEmpty()
                .extracting(BPMNMessageEvent::getEventType,
                            BPMNMessageEvent::getProcessDefinitionId,
                            BPMNMessageEvent::getProcessInstanceId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getMessagePayload().getName(),
                            event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                            event -> event.getEntity().getMessagePayload().getBusinessKey(),
                            event -> event.getEntity().getMessagePayload().getVariables()
                )
                .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_SENT,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "Test Message",
                                      "value",
                                      "businessKey",
                                      Collections.singletonMap("message_payload_variable", 
                                                               "value")));
    }

    @Test
    public void shouldReceiveCatchMessageWithCorrelationKeyAndMappedPayload() {
        // given
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withBusinessKey("businessKey")
                                                       .withVariable("correlationKey", "foo")
                                                       .withVariable("process_variable_name", "")
                                                       .withProcessDefinitionKey(CATCH_MESSAGE_PAYLOAD)
                                                       .build());
        
        // when
        processRuntime.receive(MessagePayloadBuilder.receive(TEST_MESSAGE)
                                                    .withVariable("message_variable_name", "value")
                                                    .withCorrelationKey("foo")
                                                    .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents)
                .isNotEmpty()
                .extracting(BPMNMessageEvent::getEventType,
                            BPMNMessageEvent::getProcessDefinitionId,
                            BPMNMessageEvent::getProcessInstanceId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getMessagePayload().getName(),
                            event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                            event -> event.getEntity().getMessagePayload().getBusinessKey(),
                            event -> event.getEntity().getMessagePayload().getVariables())
                .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "testMessage",
                                      "foo",
                                      process.getBusinessKey(),
                                      null),
                          Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "testMessage",
                                      "foo",
                                      process.getBusinessKey(),
                                      Collections.singletonMap("message_variable_name", 
                                                               "value")));
        
        // and 
        List<VariableInstance> variables = processRuntime.variables(ProcessPayloadBuilder.variables()
                                                         .withProcessInstanceId(process.getId())
                                                         .build());
        
        assertThat(variables).extracting(VariableInstance::getName,
                                         VariableInstance::getValue)
                             .contains(tuple("process_variable_name","value"));   
        
    }

    @Test
    public void shouldStartProcessByMessageWithMappedPayload() {
        // given
        securityUtil.logInAs("user");

        // when
        ProcessInstance process = processRuntime.start(MessagePayloadBuilder.start(START_MESSAGE_PAYLOAD)
                                                                            .withBusinessKey("businessKey")
                                                                            .withVariable("message_variable_name", "value")
                                                                            .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents).isNotEmpty()
                                  .extracting(BPMNMessageEvent::getEventType,
                                              BPMNMessageEvent::getProcessDefinitionId,
                                              BPMNMessageEvent::getProcessInstanceId,
                                              event -> event.getEntity().getProcessDefinitionId(),
                                              event -> event.getEntity().getProcessInstanceId(),
                                              event -> event.getEntity().getMessagePayload().getName(),
                                              event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                                              event -> event.getEntity().getMessagePayload().getBusinessKey(),
                                              event -> event.getEntity().getMessagePayload().getVariables())
                                  .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "startMessagePayload",
                                                        null,
                                                        process.getBusinessKey(),
                                                        Collections.singletonMap("message_variable_name",
                                                                                 "value")));
        
        // and 
        List<VariableInstance> variables = processRuntime.variables(ProcessPayloadBuilder.variables()
                                                         .withProcessInstanceId(process.getId())
                                                         .build());
        
        assertThat(variables).extracting(VariableInstance::getName,
                                         VariableInstance::getValue)
                             .contains(tuple("process_variable_name","value"));   
        
    }
     
    @Test
    public void shouldStartProcessByMessage() {
        // given
        securityUtil.logInAs("user");

        // when
        ProcessInstance process = processRuntime.start(MessagePayloadBuilder.start(TEST_MESSAGE)
                                                                            .withBusinessKey("businessKey")
                                                                            .withVariable("key", "value")
                                                                            .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents)
                .isNotEmpty()
                .extracting(BPMNMessageEvent::getEventType,
                            BPMNMessageEvent::getProcessDefinitionId,
                            BPMNMessageEvent::getProcessInstanceId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getMessagePayload().getName(),
                            event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                            event -> event.getEntity().getMessagePayload().getBusinessKey(),
                            event -> event.getEntity().getMessagePayload().getVariables())
                .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "testMessage",
                                      null,
                                      process.getBusinessKey(),
                                      Collections.singletonMap("key", 
                                                               "value")));
    }
      
    @Test
    public void shouldReceiveCatchMessageWithCorrelationKey() {
        // given
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withBusinessKey("businessKey")
                                                       .withVariable("correlationKey", "foo")
                                                       .withProcessDefinitionKey(CATCH_MESSAGE)
                                                       .build());
        
        // when
        processRuntime.receive(MessagePayloadBuilder.receive(TEST_MESSAGE)
                                                    .withVariable("key", "value")
                                                    .withCorrelationKey("foo")
                                                    .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents)
                .isNotEmpty()
                .extracting(BPMNMessageEvent::getEventType,
                            BPMNMessageEvent::getProcessDefinitionId,
                            BPMNMessageEvent::getProcessInstanceId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getMessagePayload().getName(),
                            event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                            event -> event.getEntity().getMessagePayload().getBusinessKey(),
                            event -> event.getEntity().getMessagePayload().getVariables())
                .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "testMessage",
                                      "foo",
                                      process.getBusinessKey(),
                                      null),
                          Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "testMessage",
                                      "foo",
                                      process.getBusinessKey(),
                                      Collections.singletonMap("key", 
                                                               "value")));
    }
      
    @Test
    public void shouldThrowEndMessageEvent() {
        // given
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withBusinessKey("businessKey")
                                                       .withProcessDefinitionKey(END_MESSAGE)
                                                       .build());
        
        // when
        // then
        assertThat(MessageTestConfiguration.messageEvents).isNotEmpty()
                                  .extracting(BPMNMessageEvent::getEventType,
                                              BPMNMessageEvent::getProcessDefinitionId,
                                              BPMNMessageEvent::getProcessInstanceId,
                                              event -> event.getEntity().getProcessDefinitionId(),
                                              event -> event.getEntity().getProcessInstanceId(),
                                              event -> event.getEntity().getMessagePayload().getName(),
                                              event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                                              event -> event.getEntity().getMessagePayload().getBusinessKey(),
                                              event -> event.getEntity().getMessagePayload().getVariables())
                                  .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_SENT,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        null,
                                                        process.getBusinessKey(),
                                                        null));
    }    
     
    @Test
    public void shouldReceiveBoundaryMessageWithCorrelationKey() {
        // given
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withBusinessKey("businessKey")
                                                       .withVariable("correlationKey", "foo")
                                                       .withProcessDefinitionKey(BOUNDARY_MESSAGE)
                                                       .build());
        
        // when
        processRuntime.receive(MessagePayloadBuilder.receive(TEST_MESSAGE)
                                                    .withVariable("key", "value")
                                                    .withCorrelationKey("foo")
                                                    .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents)
                .isNotEmpty()
                .extracting(BPMNMessageEvent::getEventType,
                            BPMNMessageEvent::getProcessDefinitionId,
                            BPMNMessageEvent::getProcessInstanceId,
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId(),
                            event -> event.getEntity().getMessagePayload().getName(),
                            event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                            event -> event.getEntity().getMessagePayload().getBusinessKey(),
                            event -> event.getEntity().getMessagePayload().getVariables())
                .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "testMessage",
                                      "foo",
                                      process.getBusinessKey(),
                                      null),
                          Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      process.getProcessDefinitionId(),
                                      process.getId(),
                                      "testMessage",
                                      "foo",
                                      process.getBusinessKey(),
                                      Collections.singletonMap("key", 
                                                               "value")));
    }
  
    @Test
    public void shouldReceiveSubprocessMessageWithCorrelationKey() {
        // given
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                                            .withBusinessKey("businessKey")
                                                                            .withVariable("correlationKey", "foo")
                                                                            .withProcessDefinitionKey(SUBPROCESS_MESSAGE)
                                                                            .build());

        // when
        processRuntime.receive(MessagePayloadBuilder.receive(TEST_MESSAGE)
                                                    .withVariable("key", "value")
                                                    .withCorrelationKey("foo")
                                                    .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents)
                                  .isNotEmpty()
                                  .extracting(BPMNMessageEvent::getEventType,
                                              BPMNMessageEvent::getProcessDefinitionId,
                                              BPMNMessageEvent::getProcessInstanceId,
                                              event -> event.getEntity().getProcessDefinitionId(),
                                              event -> event.getEntity().getProcessInstanceId(),
                                              event -> event.getEntity().getMessagePayload().getName(),
                                              event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                                              event -> event.getEntity().getMessagePayload().getBusinessKey(),
                                              event -> event.getEntity().getMessagePayload().getVariables())
                                  .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        null),
                                            Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        Collections.singletonMap("key",
                                                                                 "value")));
    }

    @Test
    public void shouldReceiveEventGatewayMessageWithCorrelationKey() {
        // given
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                                            .withBusinessKey("businessKey")
                                                                            .withVariable("correlationKey", "foo")
                                                                            .withProcessDefinitionKey(EVENT_GATEWAY_MESSAGE)
                                                                            .build());

        // when
        processRuntime.receive(MessagePayloadBuilder.receive(TEST_MESSAGE)
                                                    .withVariable("key", "value")
                                                    .withCorrelationKey("foo")
                                                    .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents).isNotEmpty()
                                  .extracting(BPMNMessageEvent::getEventType,
                                              BPMNMessageEvent::getProcessDefinitionId,
                                              BPMNMessageEvent::getProcessInstanceId,
                                              event -> event.getEntity().getProcessDefinitionId(),
                                              event -> event.getEntity().getProcessInstanceId(),
                                              event -> event.getEntity().getMessagePayload().getName(),
                                              event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                                              event -> event.getEntity().getMessagePayload().getBusinessKey(),
                                              event -> event.getEntity().getMessagePayload().getVariables())
                                  .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        null),
                                            Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        Collections.singletonMap("key",
                                                                                 "value")));
    }
     
    @Test
    public void shouldReceiveEventSubprocessMessageWithCorrelationKey() {
        // given
        securityUtil.logInAs("user");

        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                                            .withBusinessKey("businessKey")
                                                                            .withVariable("correlationKey", "foo")
                                                                            .withProcessDefinitionKey("eventSubprocessMessage")
                                                                            .build());
        // when
        processRuntime.receive(MessagePayloadBuilder.receive(TEST_MESSAGE)
                                                    .withVariable("key", "value")
                                                    .withCorrelationKey("foo")
                                                    .build());
        // then
        assertThat(MessageTestConfiguration.messageEvents).isNotEmpty()
                                  .extracting(BPMNMessageEvent::getEventType,
                                              BPMNMessageEvent::getProcessDefinitionId,
                                              BPMNMessageEvent::getProcessInstanceId,
                                              event -> event.getEntity().getProcessDefinitionId(),
                                              event -> event.getEntity().getProcessInstanceId(),
                                              event -> event.getEntity().getMessagePayload().getName(),
                                              event -> event.getEntity().getMessagePayload().getCorrelationKey(),
                                              event -> event.getEntity().getMessagePayload().getBusinessKey(),
                                              event -> event.getEntity().getMessagePayload().getVariables())
                                  .contains(Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        null),
                                            Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        Collections.singletonMap("key",
                                                                                 "value")));
    }  
 
}
