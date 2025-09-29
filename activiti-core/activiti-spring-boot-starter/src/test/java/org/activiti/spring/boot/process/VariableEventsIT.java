/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
import static org.assertj.core.api.Assertions.tuple;

import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.spring.boot.process.listener.VariableCreatedListener;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class VariableEventsIT {

    @Autowired
    private VariableCreatedListener variableCreatedListener;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @BeforeEach
    void setUp() {
        variableCreatedListener.clear();
        securityUtil.logInAs("user");
    }

    @Test
    public void should_EmmitEventsWithoutVariableValue_when_itsEphemeral() {
        //given
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start()
                .withProcessDefinitionKey("taskVariableMappingSendAll")
                .withVariable("nonEphemeralVar", "nonEphemeral")
                .withVariable("ephemeralVar", "ephemeral")
                .build()
        );

        //when
        assertThat(variableCreatedListener.getEvents())
            .filteredOn(event -> processInstance.getId().equals(event.getProcessInstanceId()))
            .extracting(
                event -> event.getEntity().getName(),
                VariableCreatedEvent::isEphemeralVariable,
                event -> event.getEntity().isTaskVariable()
            )
            .contains(
                tuple("ephemeralVar", true, false),
                tuple("ephemeralVar", false, true), //task variable created by MAP_ALL: not ephemeral
                tuple("nonEphemeralVar", false, false),
                tuple("nonEphemeralVar", false, true)
            );
    }
}
