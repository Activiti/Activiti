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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.StartMessageDeploymentDefinition;
import org.activiti.api.process.model.StartMessageSubscription;
import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNMessageEvent;
import org.activiti.api.process.model.events.MessageSubscriptionCancelledEvent;
import org.activiti.api.process.model.events.MessageSubscriptionEvent;
import org.activiti.api.process.model.events.StartMessageDeployedEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.event.impl.StartMessageDeployedEvents;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.spring.boot.MessageTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.tasks.TaskBaseRuntime;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({ProcessRuntimeBPMNMessageIT.TestStartMessageDeployedRuntimeEventListener.class,
         ProcessRuntimeBPMNMessageIT.TestStartMessageDeployedApplicationEventListener.class})
public class ProcessRuntimeBPMNMessageIT {

    private static final String EVENT_GATEWAY_MESSAGE = "eventGatewayMessage";

    private static final String SUBPROCESS_MESSAGE = "subprocessMessage";

    private static final String BOUNDARY_MESSAGE = "boundaryMessage";

    private static final String END_MESSAGE = "endMessage";

    private static final String CATCH_MESSAGE = "catchMessage";

    private static final String TEST_MESSAGE = "testMessage";

    private static final String START_MESSAGE_PAYLOAD = "startMessagePayload";

    private static final String PROCESS_INTERMEDIATE_THROW_MESSAGE_EVENT = "intermediateThrowMessageEvent";

    private static final String CATCH_MESSAGE_PAYLOAD = "Process_catchMessagePayload";

    @TestComponent
    public static class TestStartMessageDeployedRuntimeEventListener implements ProcessRuntimeEventListener<StartMessageDeployedEvent>{
        private List<StartMessageDeployedEvent> startMessageDeployedEvents = new ArrayList<>();

        @Override
        public void onEvent(StartMessageDeployedEvent event) {
            startMessageDeployedEvents.add(event);
        }

        public StartMessageDeployedEvent[] getStartMessageDeployedEvents() {
            return startMessageDeployedEvents.toArray(new StartMessageDeployedEvent[] {});
        }
    }

    @TestComponent
    public static class TestStartMessageDeployedApplicationEventListener {
        private List<StartMessageDeployedEvent> startMessageDeployedEvents = new ArrayList<>();

        @EventListener
        public void onEvent(StartMessageDeployedEvents event) {
            startMessageDeployedEvents.addAll(event.getStartMessageDeployedEvents());
        }


        public StartMessageDeployedEvent[] getStartMessageDeployedEvents() {
            return startMessageDeployedEvents.toArray(new StartMessageDeployedEvent[] {});
        }
    }

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private LocalEventSource localEventSource;

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private TestStartMessageDeployedRuntimeEventListener startMessageDeployedRuntimeEventListener;

    @Autowired
    private TestStartMessageDeployedApplicationEventListener startMessageDeployedApplicationEventListener;

    @BeforeEach
    public void setUp() {
        localEventSource.clearEvents();
        MessageTestConfiguration.messageEvents.clear();
        securityUtil.logInAs("user");
    }

    @AfterEach
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
        localEventSource.clearEvents();
        MessageTestConfiguration.messageEvents.clear();
    }

    @Test
    public void shouldProduceStartMessageDeployedEvents() {
        StartMessageDeployedEvent[] events = startMessageDeployedRuntimeEventListener.getStartMessageDeployedEvents();

        assertThat(events).isNotEmpty()
                          .extracting(StartMessageDeployedEvent::getEntity)
                          .extracting(StartMessageDeploymentDefinition::getMessageSubscription)
                          .extracting(StartMessageSubscription::getEventName)
                          .contains("testMessage",
                                    "startMessagePayload");

        assertThat(startMessageDeployedApplicationEventListener.getStartMessageDeployedEvents()).containsExactly(events);
    }

    @Test
    public void shouldThrowIntermediateMessageEvent() {
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
                                      singletonMap("message_payload_variable", "value")));
    }

    @Test
    public void shouldReceiveCatchMessageWithCorrelationKeyAndMappedPayload() {
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
                                      singletonMap("message_variable_name",
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
                                                        singletonMap("message_variable_name",
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
                                      singletonMap("key",
                                                               "value")));
    }

    @Test
    public void shouldReceiveCatchMessageWithCorrelationKey() {
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
                                      singletonMap("key",
                                                               "value")));
    }

    @Test
    public void shouldThrowEndMessageEvent() {
        // when
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withBusinessKey("businessKey")
                                                       .withProcessDefinitionKey(END_MESSAGE)
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
                                      singletonMap("key",
                                                               "value")));
    }

    @Test
    public void shouldReceiveSubprocessMessageWithCorrelationKey() {
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
                                                        singletonMap("key",
                                                                                 "value")));
    }

    @Test
    public void shouldReceiveEventGatewayMessageWithCorrelationKey() {
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
                                                        singletonMap("key",
                                                                                 "value")));
    }

    @Test
    public void shouldReceiveEventSubprocessMessageWithCorrelationKey() {
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
                                                        singletonMap("key",
                                                                                 "value")));
    }

    @Test
    public void shouldTestCatchMessageExpressionWithVariableMappingExtensions() {
        // when
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                                            .withBusinessKey("businessKey")
                                                                            .withProcessDefinitionKey("testCatchMessageExpression")
                                                                            .build());

        // then
        List<VariableInstance> variables = processRuntime.variables(ProcessPayloadBuilder.variables()
                                                                                         .withProcessInstance(process)
                                                                                         .build());

        assertThat(variables).extracting(VariableInstance::getName,
                                         VariableInstance::getValue)
                             .containsOnly(tuple("intermediate_var", ""),
                                           tuple("inter_message_var", "check2"));

        // when
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 2),
                                             TaskPayloadBuilder.tasks()
                                                               .withProcessInstanceId(process.getId())
                                                               .build());
        // then
        assertThat(tasks.getContent()).hasSize(1);

        String taskId = tasks.getContent()
                             .get(0)
                             .getId();

        // when
        taskRuntime.complete(TaskPayloadBuilder.complete()
                                               .withTaskId(taskId)
                                               .withVariable("Text0739ze", "foo")
                                               .build());

        // then
        variables = processRuntime.variables(ProcessPayloadBuilder.variables()
                                             .withProcessInstance(process)
                                             .build());

        assertThat(variables).extracting(VariableInstance::getName,
                                         VariableInstance::getValue)
                             .containsOnly(tuple("intermediate_var", "foo"),
                                           tuple("inter_message_var", "check2"));

        // when
        processRuntime.receive(MessagePayloadBuilder.receive("intermediate-catch-message-check2")
                                                    .withCorrelationKey("foo")
                                                    .build());

        // then
        tasks = taskRuntime.tasks(Pageable.of(0, 2),
                                  TaskPayloadBuilder.tasks()
                                                    .withProcessInstanceId(process.getId())
                                                    .build());

        assertThat(tasks.getContent()).hasSize(1);

        taskId = tasks.getContent()
                      .get(0)
                      .getId();

        // then
        taskRuntime.complete(TaskPayloadBuilder.complete()
                                               .withTaskId(taskId)
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
                                                        "intermediate-catch-message-check2",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        null),
                                            Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "intermediate-catch-message-check2",
                                                        "foo",
                                                        process.getBusinessKey(),
                                                        null));
    }

    @Test
    public void shouldTestBoundaryMessageExpression() {
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                                            .withVariable("correlationKey", "correlationId")
                                                                            .withProcessDefinitionKey("testBoundaryMessageExpression")
                                                                            .build());
        // when
        processRuntime.receive(MessagePayloadBuilder.receive("int-boundary-message")
                                                    .withCorrelationKey("correlationId")
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
                                                        "int-boundary-message",
                                                        "correlationId",
                                                        process.getBusinessKey(),
                                                        null),
                                            Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "int-boundary-message",
                                                        "correlationId",
                                                        process.getBusinessKey(),
                                                        null));
    }


    @Test
    public void shouldTestBoundaryMessageExpressionWithNotMatchingCorrelationKey() {
        processRuntime.start(ProcessPayloadBuilder.start()
                                                  .withVariable("correlationKey", "correlationId")
                                                  .withProcessDefinitionKey("testBoundaryMessageExpression")
                                                  .build());
        // when
        Throwable thrown = catchThrowable(() -> {
            processRuntime.receive(MessagePayloadBuilder.receive("int-boundary-message")
                                                        .withCorrelationKey(null)
                                                        .build());
        });

        // then
        assertThat(thrown).isInstanceOf(ActivitiObjectNotFoundException.class);
    }

    @Test
    public void shouldTestBoundaryMessageExpressionWithNotFoundMessageEventSubscription() {
        processRuntime.start(ProcessPayloadBuilder.start()
                                                  .withVariable("correlationKey", "correlationId")
                                                  .withProcessDefinitionKey("testBoundaryMessageExpression")
                                                  .build());

        // when
        Throwable thrown = catchThrowable(() -> {
        processRuntime.receive(MessagePayloadBuilder.receive("non-found-boundary-message")
                                                    .withCorrelationKey("correlationId")
                                                    .build());
        });

        // then
        assertThat(thrown).isInstanceOf(ActivitiObjectNotFoundException.class);
    }

    @Test
    public void should_getMessageSubscriptionCancelledEvent_when_processIsDeleted() {
        //when
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                       .withBusinessKey("businessKey")
                                                       .withVariable("correlationKey", "correlationKey")
                                                       .withProcessDefinitionKey(CATCH_MESSAGE)
                                                       .build());

        //then
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
                                                        "correlationKey",
                                                        process.getBusinessKey(),
                                                        null));

        //when
        processRuntime.delete(ProcessPayloadBuilder.delete(process.getId()));

        //then
        assertThat(MessageTestConfiguration.messageSubscriptionCancelledEvents).isNotEmpty()
                                  .extracting(MessageSubscriptionCancelledEvent::getEventType,
                                              MessageSubscriptionCancelledEvent::getProcessDefinitionId,
                                              MessageSubscriptionCancelledEvent::getProcessInstanceId,
                                              event -> event.getEntity().getProcessDefinitionId(),
                                              event -> event.getEntity().getProcessInstanceId(),
                                              event -> event.getEntity().getEventName(),
                                              event -> event.getEntity().getConfiguration(),
                                              event -> event.getEntity().getBusinessKey()
                                              )
                                  .contains(Tuple.tuple(MessageSubscriptionEvent.MessageSubscriptionEvents.MESSAGE_SUBSCRIPTION_CANCELLED,
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        process.getProcessDefinitionId(),
                                                        process.getId(),
                                                        "testMessage",
                                                        "correlationKey",
                                                        process.getBusinessKey()));
    }

    @Test
    public void should_cancelTask_when_interruptedMessageReceived() {

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
                .withBusinessKey("businessKey")
                .withVariable("correlationKey", "correlationKey")
                .withProcessDefinitionKey("messageInterruptingSubProcess")
                .build());

        List<Task> allTasks = taskRuntime.tasks(Pageable.of(0, 2),
                TaskPayloadBuilder.tasks()
                        .withProcessInstanceId(processInstance.getId())
                        .build()).getContent();
        assertThat(allTasks.size()).isEqualTo(1);

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
                        processInstance.getProcessDefinitionId(),
                        processInstance.getId(),
                        processInstance.getProcessDefinitionId(),
                        processInstance.getId(),
                        "interruptedMessage",
                        "correlationKey",
                        processInstance.getBusinessKey(),
                        null));
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED))
                .extracting(RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName())
                .containsExactly(tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, allTasks.get(0).getName()));


        // sending the Interrupted Start Message to process
        processRuntime.receive(MessagePayloadBuilder.receive("interruptedMessage")
                .withCorrelationKey("correlationKey")
                .build());


        //then
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
                .contains(
                        Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                "interruptedMessage",
                                "correlationKey",
                                processInstance.getBusinessKey(),
                                null),
                        Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                "interruptedMessage",
                                "correlationKey",
                                processInstance.getBusinessKey(),
                                null)
                );

        // then
        assertThat(taskBaseRuntime.getTasksByProcessInstanceId(processInstance.getId())).isEmpty();

        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED))
                .extracting(RuntimeEvent::getEventType, event -> ((Task) event.getEntity()).getName())
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED, allTasks.get(0).getName())
                );

        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void should_not_cancelTask_when_nonInterruptedMessageReceived() {

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
                .withBusinessKey("businessKey")
                .withVariable("correlationKey", "correlationKey")
                .withProcessDefinitionKey("messageNonInterruptingSubProcess")
                .build());

        List<Task> allTasks = taskRuntime.tasks(Pageable.of(0, 2),
                TaskPayloadBuilder.tasks()
                        .withProcessInstanceId(processInstance.getId())
                        .build()).getContent();
        assertThat(allTasks.size()).isEqualTo(1);

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
                        processInstance.getProcessDefinitionId(),
                        processInstance.getId(),
                        processInstance.getProcessDefinitionId(),
                        processInstance.getId(),
                        "nonInterruptedMessage",
                        "correlationKey",
                        processInstance.getBusinessKey(),
                        null));

        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED))
                .extracting(RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName())
                .containsExactly(tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, allTasks.get(0).getName()));

        // sending the Non Interrupted Start Message to process
        processRuntime.receive(MessagePayloadBuilder.receive("nonInterruptedMessage")
                .withCorrelationKey("correlationKey")
                .build());


        //then
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
                .contains(
                        Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_WAITING,
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                "nonInterruptedMessage",
                                "correlationKey",
                                processInstance.getBusinessKey(),
                                null),
                        Tuple.tuple(BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED,
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                processInstance.getProcessDefinitionId(),
                                processInstance.getId(),
                                "nonInterruptedMessage",
                                "correlationKey",
                                processInstance.getBusinessKey(),
                                null)
                );

        assertThat(taskBaseRuntime.getTasksByProcessInstanceId(processInstance.getId()).size()).isEqualTo(1);
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED))
                .extracting(RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName())
                .containsExactly(tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, allTasks.get(0).getName()));

        localEventSource.clearEvents();

        taskBaseRuntime.completeTask(allTasks.get(0).getId());

        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                .extracting(RuntimeEvent::getEventType, event -> ((Task) event.getEntity()).getName())
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED, allTasks.get(0).getName())
                );

        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

}
