/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.runtime.api.event.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToAPIProcessStartedEventConverterTest {

    @Mock
    private APIProcessInstanceConverter processInstanceConverter;

    @Mock
    private ActivitiProcessStartedEvent internalEvent;

    @Mock
    private ExecutionEntity executionEntity;

    private ToAPIProcessStartedEventConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ToAPIProcessStartedEventConverter(processInstanceConverter);
    }

    @Test
    void shouldConvertToProcessStartedEventWhenInternalEventIsValid() {
        ProcessInstanceImpl processInstance = new ProcessInstanceImpl();
        processInstance.setId("processInstanceId");

        when(internalEvent.getEntity()).thenReturn(executionEntity);
        when(executionEntity.getProcessInstance()).thenReturn(executionEntity);
        when(processInstanceConverter.from(executionEntity)).thenReturn(processInstance);

        when(internalEvent.getNestedProcessDefinitionId()).thenReturn("nestedProcDefId");
        when(internalEvent.getNestedProcessInstanceId()).thenReturn("nestedProcInstId");
        when(internalEvent.getLinkedProcessInstanceId()).thenReturn("linkedProcInstId");
        when(internalEvent.getLinkedProcessInstanceType()).thenReturn("linkedProcInstType");

        Optional<ProcessStartedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        ProcessStartedEvent event = result.get();
        ProcessInstanceImpl eventProcessInstance = (ProcessInstanceImpl) event.getEntity();
        assertThat(eventProcessInstance.getId()).isEqualTo(processInstance.getId());
        assertThat(event.getNestedProcessDefinitionId()).isEqualTo("nestedProcDefId");
        assertThat(event.getNestedProcessInstanceId()).isEqualTo("nestedProcInstId");
        assertThat(event.getLinkedProcessInstanceId()).isEqualTo("linkedProcInstId");
        assertThat(event.getLinkedProcessInstanceType()).isEqualTo("linkedProcInstType");
    }
}
