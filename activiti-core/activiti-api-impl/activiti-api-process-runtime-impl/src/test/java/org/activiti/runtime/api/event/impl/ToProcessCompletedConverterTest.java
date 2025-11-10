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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToProcessCompletedConverterTest {

    @Mock
    private APIProcessInstanceConverter processInstanceConverter;

    @Mock
    private ActivitiEntityEvent internalEvent;

    @Mock
    private ExecutionEntity executionEntity;

    private ToProcessCompletedConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ToProcessCompletedConverter(processInstanceConverter);
    }

    @Test
    void shouldConvertToProcessCompletedEventWhenInternalEventIsValid() {

        ProcessInstanceImpl processInstance = new ProcessInstanceImpl();
        processInstance.setId("processInstanceId");


        when(internalEvent.getEntity()).thenReturn(executionEntity);
        when(executionEntity.getProcessInstance()).thenReturn(executionEntity);
        when(processInstanceConverter.from(executionEntity)).thenReturn(processInstance);
        when(internalEvent.getActor()).thenReturn("testActor");

        Optional<ProcessCompletedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        assertThat(result.get().getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(result.get().getActor()).isEqualTo("testActor");
    }
}
