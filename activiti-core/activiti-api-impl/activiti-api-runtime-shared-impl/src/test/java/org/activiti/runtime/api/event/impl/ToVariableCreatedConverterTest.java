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

import static org.activiti.runtime.api.event.impl.VariableBuilder.buildVariableEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableEvent.VariableEvents;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToVariableCreatedConverterTest {

    @Mock
    private EphemeralVariableResolver ephemeralVariableResolver;

    @InjectMocks
    private ToVariableCreatedConverter converter;

    @Test
    void should_convertToVariableCreatedEvent() {
        ActivitiVariableEventImpl internalEvent = buildVariableEvent();

        Optional<VariableCreatedEvent> result = converter.from(internalEvent);
        assertThat(result).isPresent();
        VariableCreatedEvent actualEvent = result.get();
        assertThat(actualEvent.isEphemeralVariable()).isFalse();

        VariableInstance actualEntity = assertVariableCreatedEvent(actualEvent, internalEvent);
        Object actualValue = actualEntity.getValue();
        assertThat(actualValue).isEqualTo(internalEvent.getVariableValue());
    }

    @Test
    void should_convertToVariableCreatedEvent_withNullValue_when_variableIsEphemeral() {
        ActivitiVariableEventImpl internalEvent = buildVariableEvent();

        when(ephemeralVariableResolver.isEphemeralVariable(internalEvent)).thenReturn(true);

        Optional<VariableCreatedEvent> result = converter.from(internalEvent);
        assertThat(result).isPresent();
        VariableCreatedEvent actualEvent = result.get();
        assertThat(actualEvent.isEphemeralVariable()).isTrue();

        VariableInstance actualEntity = assertVariableCreatedEvent(actualEvent, internalEvent);
        Object actualValue = actualEntity.getValue();
        assertThat(actualValue).isNull();
    }

    private VariableInstance assertVariableCreatedEvent(
        VariableCreatedEvent actualEvent,
        ActivitiVariableEventImpl internalEvent
    ) {
        assertThat(actualEvent.getEventType()).isEqualTo(VariableEvents.VARIABLE_CREATED);
        assertThat(actualEvent.getProcessInstanceId()).isEqualTo(internalEvent.getProcessInstanceId());
        assertThat(actualEvent.getProcessDefinitionId()).isEqualTo(internalEvent.getProcessDefinitionId());
        VariableInstance actualEntity = actualEvent.getEntity();
        assertThat(actualEntity.getName()).isEqualTo(internalEvent.getVariableName());
        assertThat(actualEntity.getProcessInstanceId()).isEqualTo(internalEvent.getProcessInstanceId());
        assertThat(actualEntity.getTaskId()).isEqualTo(actualEvent.getEntity().getTaskId());
        assertThat(actualEntity.getType()).isEqualTo(internalEvent.getVariableType().getTypeName());
        return actualEntity;
    }
}
