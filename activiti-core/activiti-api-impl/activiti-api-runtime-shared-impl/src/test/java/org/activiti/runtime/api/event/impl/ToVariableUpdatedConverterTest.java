/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.activiti.api.model.shared.event.VariableEvent.VariableEvents;
import org.activiti.api.model.shared.event.VariableUpdatedEvent;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.engine.delegate.event.impl.ActivitiVariableUpdatedEventImpl;
import org.activiti.engine.impl.variable.IntegerType;
import org.activiti.engine.impl.variable.VariableType;
import org.junit.jupiter.api.Test;

class ToVariableUpdatedConverterTest {

    private ToVariableUpdatedConverter converter = new ToVariableUpdatedConverter();

    @Test
    void should_convertToVariableUpdatedEvent() {
        ActivitiVariableUpdatedEventImpl internalEvent = new ActivitiVariableUpdatedEventImpl();
        internalEvent.setVariableName("variableName");
        internalEvent.setProcessInstanceId("processInstanceId");
        internalEvent.setTaskId("taskId");
        VariableType variableType = new IntegerType();
        internalEvent.setVariableType(variableType);
        Object value = mock(Object.class);
        internalEvent.setVariableValue(value);
        Object previousValue = mock(Object.class);
        internalEvent.setVariablePreviousValue(previousValue);

        Optional<VariableUpdatedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        VariableUpdatedEvent actualEvent = result.get();
        assertThat(actualEvent.getEventType()).isEqualTo(VariableEvents.VARIABLE_UPDATED);
        VariableInstance actualEntity = actualEvent.getEntity();
        assertThat(actualEntity.getName()).isEqualTo("variableName");
        assertThat(actualEntity.getProcessInstanceId()).isEqualTo("processInstanceId");
        assertThat(actualEntity.getTaskId()).isEqualTo("taskId");
        assertThat(actualEntity.getType()).isEqualTo("integer");
        Object actualValue = actualEntity.getValue();
        Object actualPreviousValue = actualEvent.getPreviousValue();
        assertThat(actualPreviousValue).isSameAs(previousValue);
        assertThat(actualValue).isSameAs(value);
        assertThat(previousValue).isNotSameAs(value);
    }
}
