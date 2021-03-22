/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableUpdatedEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeEventsIT {

    private static final String SINGLE_TASK_PROCESS = "SingleTaskProcess";
    private static final String LOGGED_USER = "user";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private LocalEventSource localEventSource;

    @BeforeEach
    public void init() {
        //Reset test variables
        RuntimeTestConfiguration.processImageConnectorExecuted = false;
        RuntimeTestConfiguration.tagImageConnectorExecuted = false;
        RuntimeTestConfiguration.discardImageConnectorExecuted = false;
        //Reset event collections
        localEventSource.clearEvents();
        securityUtil.logInAs(LOGGED_USER);
    }

    @AfterEach
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void should_emitWithSameProcessInstanceForAllSequenceFlowTakenEvents() {
        //when
        ProcessInstance singleTaskProcess = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name", "peter")
                .build()
        );

        //then
        assertThat(localEventSource.getEvents(BPMNSequenceFlowTakenEvent.class))
            .extracting(RuntimeEvent::getProcessInstanceId)
            .containsExactly(singleTaskProcess.getId());
    }

    @Test
    public void should_emitSingleVariableCreatedEvent_when_createdWithVariable() {
        //when
        ProcessInstance singleTaskProcess = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name", "peter")
                .build()
        );

        //then
        List<VariableCreatedEvent> variableCreatedEvents = localEventSource.getEvents(
            VariableCreatedEvent.class
        );
        assertThat(variableCreatedEvents).hasSize(1);

        VariableCreatedEvent variableCreatedEvent = variableCreatedEvents.get(
            0
        );
        assertThat(variableCreatedEvent.getProcessInstanceId())
            .isEqualTo(singleTaskProcess.getId());
        assertThat(variableCreatedEvent.getEntity().getName())
            .isEqualTo("name");
        assertThat(variableCreatedEvent.getEntity().getType())
            .isEqualTo("string");
        assertThat(variableCreatedEvent.getEntity().<String>getValue())
            .isEqualTo("peter");
        assertThat(variableCreatedEvent.getEntity().getProcessInstanceId())
            .isEqualTo(singleTaskProcess.getId());
    }

    @Test
    public void should_emitThreeVariableCreatedEvents_when_createdWithThreeVariables() {
        //when
        ProcessInstance singleTaskProcess = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariables(
                    Map.ofEntries(
                        Map.entry("name", "peter"),
                        Map.entry("active", true),
                        Map.entry("age", 25)
                    )
                )
                .build()
        );

        //then
        List<VariableCreatedEvent> variableCreatedEvents = localEventSource.getEvents(
            VariableCreatedEvent.class
        );
        assertThat(variableCreatedEvents)
            .hasSize(3)
            .extracting(RuntimeEvent::getProcessInstanceId)
            .containsOnly(singleTaskProcess.getId());
        assertThat(variableCreatedEvents)
            .extracting(event -> event.getEntity().getType())
            .containsExactly("string", "boolean", "integer");
    }

    @Test
    public void should_emitSingleVariableUpdatedEvent_when_updatedWithVariable() {
        //given
        ProcessInstance singleTaskProcess = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name", "peter")
                .build()
        );

        //when
        processRuntime.setVariables(
            ProcessPayloadBuilder
                .setVariables(singleTaskProcess)
                .withVariable("name", "paul")
                .withProcessInstance(singleTaskProcess)
                .build()
        );

        //then
        List<VariableUpdatedEvent> variableCreatedEvents = localEventSource.getEvents(
            VariableUpdatedEvent.class
        );
        assertThat(variableCreatedEvents).hasSize(1);

        VariableUpdatedEvent variableUpdatedEvent = variableCreatedEvents.get(
            0
        );
        assertThat(variableUpdatedEvent.getEntity().getName())
            .isEqualTo("name");
        assertThat(variableUpdatedEvent.getEntity().getType())
            .isEqualTo("string");
        assertThat(variableUpdatedEvent.getEntity().<String>getValue())
            .isEqualTo("paul");
        assertThat(variableUpdatedEvent.<String>getPreviousValue())
            .isEqualTo("peter");
        assertThat(variableUpdatedEvent.getEntity().getProcessInstanceId())
            .isEqualTo(singleTaskProcess.getId());
    }

    @Test
    public void should_emmitEventOnProcessDeletion() {
        //given
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withName("to be deleted")
                .withBusinessKey("my business key")
                .build()
        );

        //when
        processRuntime.delete(ProcessPayloadBuilder.delete(processInstance));

        //then
        List<ProcessCancelledEvent> processCancelledEvents = localEventSource.getEvents(
            ProcessCancelledEvent.class
        );
        assertThat(processCancelledEvents).hasSize(1);

        ProcessCancelledEvent processCancelledEvent = processCancelledEvents.get(
            0
        );
        assertThat(processCancelledEvent.getCause())
            .isEqualTo("process instance deleted");
        assertThat(processCancelledEvent.getEntity().getId())
            .isEqualTo(processInstance.getId());
        assertThat(processCancelledEvent.getEntity().getProcessDefinitionId())
            .isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(processCancelledEvent.getEntity().getName())
            .isEqualTo(processInstance.getName());
        assertThat(processCancelledEvent.getEntity().getBusinessKey())
            .isEqualTo(processInstance.getBusinessKey());
        assertThat(processCancelledEvent.getEntity().getStartDate())
            .isEqualTo(processInstance.getStartDate());
        assertThat(processCancelledEvent.getEntity().getInitiator())
            .isEqualTo(LOGGED_USER);
    }
}
