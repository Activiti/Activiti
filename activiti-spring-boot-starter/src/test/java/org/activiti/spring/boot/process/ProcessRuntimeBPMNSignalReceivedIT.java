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

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNSignalEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.boot.process.listener.DummyBPMNSignalReceivedListener;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeBPMNSignalReceivedIT {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private DummyBPMNSignalReceivedListener listener;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Before
    public void setUp() {
        listener.clear();
    }

    @After
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void shouldGetSignalReceivedEventsForProcessWithSignalStart() {

        //In this test processWithSignalStart1 should be started
        //given
        securityUtil.logInAs("salaboy");
        Page<ProcessDefinition> processDefinitionPage = processRuntime
                .processDefinitions(Pageable.of(0,
                                                10),
                                    ProcessPayloadBuilder
                                            .processDefinitions()
                                            .withProcessDefinitionKey("processWithSignalStart1")
                                            .build());
        assertThat(processDefinitionPage.getContent()).hasSize(1);

        //when
        SignalPayload signalPayload = new SignalPayload("The Signal",
                                                        null);
        processRuntime.signal(signalPayload);

        //then
        String processDefinitionId = processDefinitionPage.getContent().get(0).getId();
        assertThat(listener.getSignalReceivedEvents())
                .extracting(BPMNSignalReceivedEvent::getEventType,
                            BPMNSignalReceivedEvent::getProcessDefinitionId,
                            event -> event.getEntity().getSignalPayload().getName(),
                            event -> event.getEntity().getElementId(),
                            event -> event.getEntity().getProcessDefinitionId()
                )
                .contains(Tuple.tuple(BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                                      processDefinitionId,
                                      "The Signal",
                                      "theStart",
                                      processDefinitionId
                ));
    }

    @Test
    public void shouldGetOneSignalReceivedEventPerWaitingSignalsForNonStartSignals() {

        //given
        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance process1 = processRuntime.start(ProcessPayloadBuilder.start()
                                                                .withProcessDefinitionKey("ProcessWithBoundarySignal")
                                                                .build());

        ProcessInstance process2 = processRuntime.start(ProcessPayloadBuilder.start()
                                                                .withProcessDefinitionKey("ProcessWithBoundarySignal")
                                                                .build());

        SignalPayload signalPayload = ProcessPayloadBuilder.signal().withName("go").build();
        processRuntime.signal(signalPayload);

        //then
        assertThat(listener.getSignalReceivedEvents())
                .isNotEmpty()
                .hasSize(2);

        assertThat(listener.getSignalReceivedEvents())
                .extracting(BPMNSignalReceivedEvent::getEventType,
                            BPMNSignalReceivedEvent::getProcessDefinitionId,
                            BPMNSignalReceivedEvent::getProcessInstanceId,
                            event -> event.getEntity().getSignalPayload().getName(),
                            event -> event.getEntity().getElementId(),
                            event -> event.getEntity().getProcessDefinitionId(),
                            event -> event.getEntity().getProcessInstanceId()
                            )
                .contains(
                        tuple(BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                              process1.getProcessDefinitionId(),
                              process1.getId(),
                              "go",
                              "sid-6220E76D-719E-4C05-A664-BC186E50D477",
                              process1.getProcessDefinitionId(),
                              process1.getId()
                        ),
                        tuple(BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                              process2.getProcessDefinitionId(),
                              process2.getId(),
                              "go",
                              "sid-6220E76D-719E-4C05-A664-BC186E50D477",
                              process2.getProcessDefinitionId(),
                              process2.getId()
                        )
                );
    }

    @Test
    public void shouldGetSignalReceivedEventWithVariables() {

        //given
        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                               .withProcessDefinitionKey("ProcessWithBoundarySignal")
                                                               .withVariable("name",
                                                                             "peter")
                                                               .build());

        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName("go")
                .withVariable("signal-variable",
                              "test")
                .build();
        processRuntime.signal(signalPayload);

        //then
        assertThat(listener.getSignalReceivedEvents())
                .isNotEmpty()
                .hasSize(1);

        BPMNSignalReceivedEvent event = listener.getSignalReceivedEvents().iterator().next();

        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo(process.getId());
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo("go");
        assertThat(event.getEntity().getSignalPayload().getVariables().size()).isEqualTo(1);
        assertThat(event.getEntity().getSignalPayload().getVariables().get("signal-variable")).isEqualTo("test");
    }
}
