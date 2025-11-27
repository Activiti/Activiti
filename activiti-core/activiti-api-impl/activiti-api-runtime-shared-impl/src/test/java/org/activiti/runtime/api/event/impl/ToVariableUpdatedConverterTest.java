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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.activiti.api.model.shared.event.VariableEvent.VariableEvents;
import org.activiti.api.model.shared.event.VariableUpdatedEvent;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.engine.delegate.event.impl.ActivitiVariableUpdatedEventImpl;
import org.activiti.engine.impl.variable.IntegerType;
import org.activiti.engine.impl.variable.VariableType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToVariableUpdatedConverterTest {

    @Mock
    private EphemeralVariableResolver ephemeralVariableResolver;

    @InjectMocks
    private ToVariableUpdatedConverter converter;

    @Test
    void should_convertToVariableUpdatedEvent() {
        ActivitiVariableUpdatedEventImpl internalEvent = buildVariableUpdatedEvent();

        Optional<VariableUpdatedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        VariableUpdatedEvent actualEvent = result.get();
        assertThat(actualEvent.isEphemeralVariable()).isFalse();

        VariableInstance actualEntity = assertVariableUpdatedEvent(actualEvent, internalEvent);

        Object actualValue = actualEntity.getValue();
        Object actualPreviousValue = actualEvent.getPreviousValue();
        assertThat(actualPreviousValue).isSameAs(100);
        assertThat(actualValue).isSameAs(50);
        assertThat(actualValue).isNotSameAs(actualPreviousValue);
    }

    @Test
    void should_convertToVariableUpdatedEvent_withNullValue_when_variableIsEphemeral() {
        ActivitiVariableUpdatedEventImpl internalEvent = buildVariableUpdatedEvent();

        when(ephemeralVariableResolver.isEphemeralVariable(internalEvent)).thenReturn(true);

        Optional<VariableUpdatedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        VariableUpdatedEvent actualEvent = result.get();
        assertThat(actualEvent.isEphemeralVariable()).isTrue();

        VariableInstance actualEntity = assertVariableUpdatedEvent(actualEvent, internalEvent);

        Object actualValue = actualEntity.getValue();
        Object actualPreviousValue = actualEvent.getPreviousValue();
        assertThat(actualPreviousValue).isNull();
        assertThat(actualValue).isNull();
    }

    private VariableInstance assertVariableUpdatedEvent(
        VariableUpdatedEvent actualEvent,
        ActivitiVariableUpdatedEventImpl internalEvent
    ) {
        assertThat(actualEvent.getEventType()).isEqualTo(VariableEvents.VARIABLE_UPDATED);
        VariableInstance actualEntity = actualEvent.getEntity();
        assertThat(actualEntity.getName()).isEqualTo(internalEvent.getVariableName());
        assertThat(actualEntity.getProcessInstanceId()).isEqualTo(internalEvent.getProcessInstanceId());
        assertThat(actualEntity.getTaskId()).isEqualTo(internalEvent.getTaskId());
        assertThat(actualEntity.getType()).isEqualTo(internalEvent.getVariableType().getTypeName());
        return actualEntity;
    }

    private ActivitiVariableUpdatedEventImpl buildVariableUpdatedEvent() {
        ActivitiVariableUpdatedEventImpl internalEvent = new ActivitiVariableUpdatedEventImpl();
        internalEvent.setVariableName("variableName");
        internalEvent.setProcessInstanceId("processInstanceId");
        internalEvent.setProcessDefinitionId("processDefinitionId");
        VariableType variableType = new IntegerType();
        internalEvent.setVariableType(variableType);
        internalEvent.setVariableValue(50);
        internalEvent.setVariablePreviousValue(100);
        return internalEvent;
    }
}
