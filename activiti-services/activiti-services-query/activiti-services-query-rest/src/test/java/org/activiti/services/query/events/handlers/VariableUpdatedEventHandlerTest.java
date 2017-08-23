/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.query.events.handlers;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.model.Variable;
import org.activiti.services.query.events.VariableUpdatedEvent;
import org.activiti.test.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class VariableUpdatedEventHandlerTest {

    @InjectMocks
    private VariableUpdatedEventHandler handler;

    @Mock
    private ProcessVariableUpdateHandler processVariableUpdateHandler;

    @Mock
    private TaskVariableUpdatedHandler taskVariableUpdatedHandler;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleShouldUseProcessVariableUpdateHandlerWhenNoTaskId() throws Exception {
        //given
        VariableUpdatedEvent event = new VariableUpdatedEvent(System.currentTimeMillis(),
                                                              "variableUpdated",
                                                              "10",
                                                              "20",
                                                              "30",
                                                              "var",
                                                              "v1",
                                                              "string",
                                                              null);

        //when
        handler.handle(event);

        //then
        ArgumentCaptor<Variable> captor = ArgumentCaptor.forClass(Variable.class);
        verify(processVariableUpdateHandler).handle(captor.capture());

        Variable variable = captor.getValue();
        Assertions.assertThat(variable)
                .hasProcessInstanceId("30")
                .hasName("var")
                .hasValue("v1")
                .hasType("string");
    }

    @Test
    public void handleShouldUseTaskVariableUpdateHandlerWhenTaskIdIsSet() throws Exception {
        //given
        VariableUpdatedEvent event = new VariableUpdatedEvent(System.currentTimeMillis(),
                                                              "variableUpdated",
                                                              "10",
                                                              "20",
                                                              "30",
                                                              "var",
                                                              "v1",
                                                              "string",
                                                              "40");

        //when
        handler.handle(event);

        //then
        ArgumentCaptor<Variable> captor = ArgumentCaptor.forClass(Variable.class);
        verify(taskVariableUpdatedHandler).handle(captor.capture());

        Variable variable = captor.getValue();
        Assertions.assertThat(variable)
                .hasTaskId("40")
                .hasName("var")
                .hasValue("v1")
                .hasType("string");
    }

    @Test
    public void getHandledEventClass() throws Exception {
        //when
        Class<? extends ProcessEngineEvent> handledEventClass = handler.getHandledEventClass();

        //then
        assertThat(handledEventClass).isEqualTo(VariableUpdatedEvent.class);
    }
}