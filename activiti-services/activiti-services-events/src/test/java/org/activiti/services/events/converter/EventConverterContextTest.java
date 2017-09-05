
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

package org.activiti.services.events.converter;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class EventConverterContextTest {

    private EventConverterContext converterContext;

    private Map<ActivitiEventType, EventConverter> converters;

    @Mock
    private EventConverter eventConverter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        converters = new HashMap<>();
        converterContext = new EventConverterContext(converters);
    }

    @Test
    public void shouldChooseConverterBaseOnEventType() throws Exception {
        //given
        ActivitiEventImpl activitiEvent = new ActivitiEventImpl(ActivitiEventType.PROCESS_STARTED);
        ProcessEngineEvent processEngineEvent = mock(ProcessEngineEvent.class);

        converters.put(ActivitiEventType.PROCESS_STARTED, eventConverter);
        given(eventConverter.from(activitiEvent)).willReturn(processEngineEvent);

        //when
        ProcessEngineEvent eventResult = converterContext.from(activitiEvent);

        //then
        assertThat(eventResult).isEqualTo(processEngineEvent);
    }

    @Test
    public void shouldReturnNullWhenNoConverterIsFoundForTheGivenType() throws Exception {
        //given
        ActivitiEventImpl activitiEvent = new ActivitiEventImpl(ActivitiEventType.PROCESS_STARTED);

        //when
        ProcessEngineEvent eventResult = converterContext.from(activitiEvent);

        //then
        assertThat(eventResult).isNull();
    }
}