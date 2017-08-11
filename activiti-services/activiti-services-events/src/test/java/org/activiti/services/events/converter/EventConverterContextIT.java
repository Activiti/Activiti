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

import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EventConverterContextIT.EventConverterContextConfig.class)
@TestPropertySource("classpath:test-application.properties")
public class EventConverterContextIT {

    @Autowired
    private EventConverterContext converterContext;

    @Configuration
    @ComponentScan({"org.activiti.services.events.converter", "org.activiti.services.core.model.converter"})
    public static class EventConverterContextConfig {

    }

    @Test
    public void shouldHandleAllSupportedEvents() throws Exception {
        //when
        Map<ActivitiEventType, EventConverter> converters = converterContext.getConvertersMap();

        //then
        assertThat(converters).containsOnlyKeys(ActivitiEventType.ACTIVITY_CANCELLED,
                                                ActivitiEventType.ACTIVITY_COMPLETED,
                                                ActivitiEventType.ACTIVITY_STARTED,
                                                ActivitiEventType.PROCESS_CANCELLED,
                                                ActivitiEventType.PROCESS_COMPLETED,
                                                ActivitiEventType.PROCESS_STARTED,
                                                ActivitiEventType.SEQUENCEFLOW_TAKEN,
                                                ActivitiEventType.TASK_ASSIGNED,
                                                ActivitiEventType.TASK_COMPLETED,
                                                ActivitiEventType.TASK_CREATED,
                                                ActivitiEventType.VARIABLE_CREATED,
                                                ActivitiEventType.VARIABLE_DELETED,
                                                ActivitiEventType.VARIABLE_UPDATED);
    }

    @Test
    public void shouldIncludeApplicationNameInConvertedEvents() throws Exception {

        //when
        Map<ActivitiEventType, EventConverter> converters = converterContext.getConvertersMap();

        //then
        assertThat(converters).containsKey(ActivitiEventType.PROCESS_STARTED);
        ActivitiProcessStartedEvent activitiEvent = mock(ActivitiProcessStartedEvent.class);

        ProcessEngineEvent processEngineEvent = converters.get(ActivitiEventType.PROCESS_STARTED).from(activitiEvent);

        assertThat(processEngineEvent).isNotNull();
        assertThat(processEngineEvent.getApplicationName()).isNotEmpty();
        // this comes from the application.properties (test-application.properties) spring app name configuration
        assertThat(processEngineEvent.getApplicationName()).isEqualTo("test-app");
    }
}