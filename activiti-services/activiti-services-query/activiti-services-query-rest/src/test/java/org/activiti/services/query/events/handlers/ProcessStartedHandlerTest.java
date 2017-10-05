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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.events.ProcessStartedEvent;
import org.activiti.services.query.model.ProcessInstance;
import org.activiti.test.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ProcessStartedHandlerTest {

    @InjectMocks
    private ProcessStartedHandler handler;

    @Mock
    private ProcessInstanceRepository processInstanceRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleShouldStoreANewProcessInstanceInTheRepository() throws Exception {
        //given
        ProcessStartedEvent event = new ProcessStartedEvent(System.currentTimeMillis(),
                                                            "ProcessStartedEvent",
                                                            "10",
                                                            "100",
                                                            "200",
                                                            "101",
                                                            "201");

        //when
        handler.handle(event);

        //then
        ArgumentCaptor<ProcessInstance> argumentCaptor = ArgumentCaptor.forClass(ProcessInstance.class);
        verify(processInstanceRepository).save(argumentCaptor.capture());

        ProcessInstance processInstance = argumentCaptor.getValue();
        Assertions.assertThat(processInstance)
                .hasProcessInstanceId("200")
                .hasProcessDefinitionId("100")
                .hasStatus("RUNNING");
    }

    @Test
    public void getHandledEventClassShouldReturnProcessStartedEvent() throws Exception {
        //when
        Class<? extends ProcessEngineEvent> handledEventClass = handler.getHandledEventClass();

        //then
        assertThat(handledEventClass).isEqualTo(ProcessStartedEvent.class);
    }
}