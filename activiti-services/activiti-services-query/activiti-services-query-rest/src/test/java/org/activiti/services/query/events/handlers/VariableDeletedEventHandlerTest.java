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
import org.activiti.services.query.events.VariableDeletedEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class VariableDeletedEventHandlerTest {

    @InjectMocks
    private VariableDeletedEventHandler handler;

    @Mock
    private ProcessVariableDeletedHandler processVariableDeletedHandler;

    @Mock
    private TaskVariableDeletedHandler taskVariableDeletedHandler;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleShouldUseProcessVariableDeleteHandlerWhenNoTaskId() throws Exception {
        //given
        VariableDeletedEvent event = new VariableDeletedEvent();
        event.setTaskId(null);

        //when
        handler.handle(event);

        //then
        verify(processVariableDeletedHandler).handle(event);
    }

    @Test
    public void handleShouldUseProcessVariableDeleteHandlerWhenTaskIdIsPresent() throws Exception {
        //given
        VariableDeletedEvent event = new VariableDeletedEvent();
        event.setTaskId("1");

        //when
        handler.handle(event);

        //then
        verify(taskVariableDeletedHandler).handle(event);
    }


    @Test
    public void getHandledEventClassShouldReturnVariableDeletedEvent() throws Exception {
        //when
        Class<? extends ProcessEngineEvent> handledEventClass = handler.getHandledEventClass();

        //then
        assertThat(handledEventClass).isEqualTo(VariableDeletedEvent.class);
    }
}