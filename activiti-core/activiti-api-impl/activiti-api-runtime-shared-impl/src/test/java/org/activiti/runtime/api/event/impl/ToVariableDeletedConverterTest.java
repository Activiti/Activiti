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
import org.activiti.api.model.shared.event.VariableDeletedEvent;
import org.activiti.api.model.shared.event.VariableEvent.VariableEvents;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToVariableDeletedConverterTest {

    @Mock
    EphemeralVariableResolver ephemeralVariableResolver;

    @InjectMocks
    private ToVariableDeletedConverter converter;

    @Test
    void should_convertToVariableDeletedEvent() {
        ActivitiVariableEventImpl internalEvent = buildVariableEvent();

        Optional<VariableDeletedEvent> result = converter.from(internalEvent);
        assertThat(result).isPresent();
        VariableDeletedEvent actualEvent = result.get();
        assertThat(actualEvent.isEphemeralVariable()).isFalse();

        VariableInstance actualEntity = assertVariableDeleted(actualEvent, internalEvent);
        Object actualValue = actualEntity.getValue();
        assertThat(actualValue).isEqualTo(internalEvent.getVariableValue());
    }

    @Test
    void should_convertToVariableDeletedEvent_withNullValue_when_variableIsEphemeral() {
        ActivitiVariableEventImpl internalEvent = buildVariableEvent();

        when(ephemeralVariableResolver.isEphemeralVariable(internalEvent)).thenReturn(true);

        Optional<VariableDeletedEvent> result = converter.from(internalEvent);
        assertThat(result).isPresent();
        VariableDeletedEvent actualEvent = result.get();
        assertThat(actualEvent.isEphemeralVariable()).isTrue();

        VariableInstance actualEntity = assertVariableDeleted(actualEvent, internalEvent);
        Object actualValue = actualEntity.getValue();
        assertThat(actualValue).isNull();
    }

    private VariableInstance assertVariableDeleted(
        VariableDeletedEvent actualEvent,
        ActivitiVariableEventImpl internalEvent
    ) {
        assertThat(actualEvent.getEventType()).isEqualTo(VariableEvents.VARIABLE_DELETED);
        VariableInstance actualEntity = actualEvent.getEntity();
        assertThat(actualEntity.getName()).isEqualTo(internalEvent.getVariableName());
        assertThat(actualEntity.getProcessInstanceId()).isEqualTo(internalEvent.getProcessInstanceId());
        assertThat(actualEntity.getTaskId()).isEqualTo(internalEvent.getTaskId());
        assertThat(actualEntity.getType()).isEqualTo(internalEvent.getVariableType().getTypeName());
        return actualEntity;
    }
}
