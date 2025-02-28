/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
import org.activiti.spring.process.ProcessExtensionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ToVariableUpdatedConverterTest {
    ProcessExtensionService processExtensionService = Mockito.mock(ProcessExtensionService.class);

    private ToVariableUpdatedConverter converter = new ToVariableUpdatedConverter(processExtensionService);

    @Test
    void should_convertToVariableUpdatedEvent() {
        ActivitiVariableUpdatedEventImpl internalEvent = getActivitiVariableUpdatedEvent();

        Optional<VariableUpdatedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        VariableUpdatedEvent actualEvent = result.get();

        VariableInstance actualEntity = assertVariableUpdatedEvent(actualEvent);

        Object actualValue = actualEntity.getValue();
        Object actualPreviousValue = actualEvent.getPreviousValue();
        assertThat(actualPreviousValue).isSameAs(100);
        assertThat(actualValue).isSameAs(50);
        assertThat(actualValue).isNotSameAs(actualPreviousValue);
    }

    @Test
    void should_convertToVariableUpdatedEvent_withNullValue_when_variableIsEphemeral() {
        ActivitiVariableUpdatedEventImpl internalEvent = getActivitiVariableUpdatedEvent();

        when(processExtensionService.hasEphemeralVariable("processDefinitionId", "variableName")).thenReturn(true);

        Optional<VariableUpdatedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        VariableUpdatedEvent actualEvent = result.get();

        VariableInstance actualEntity = assertVariableUpdatedEvent(actualEvent);

        Object actualValue = actualEntity.getValue();
        Object actualPreviousValue = actualEvent.getPreviousValue();
        assertThat(actualPreviousValue).isEqualTo(100);
        assertThat(actualValue).isNull();
    }

    private VariableInstance assertVariableUpdatedEvent(VariableUpdatedEvent actualEvent) {
        assertThat(actualEvent.getEventType()).isEqualTo(VariableEvents.VARIABLE_UPDATED);
        VariableInstance actualEntity = actualEvent.getEntity();
        assertThat(actualEntity.getName()).isEqualTo("variableName");
        assertThat(actualEntity.getProcessInstanceId()).isEqualTo("processInstanceId");
        assertThat(actualEntity.getTaskId()).isEqualTo("taskId");
        assertThat(actualEntity.getType()).isEqualTo("integer");
        return actualEntity;
    }

    private ActivitiVariableUpdatedEventImpl getActivitiVariableUpdatedEvent() {
        ActivitiVariableUpdatedEventImpl internalEvent = new ActivitiVariableUpdatedEventImpl();
        internalEvent.setVariableName("variableName");
        internalEvent.setProcessInstanceId("processInstanceId");
        internalEvent.setProcessDefinitionId("processDefinitionId");
        internalEvent.setTaskId("taskId");
        VariableType variableType = new IntegerType();
        internalEvent.setVariableType(variableType);
        internalEvent.setVariableValue(50);
        internalEvent.setVariablePreviousValue(100);
        return internalEvent;
    }
}
