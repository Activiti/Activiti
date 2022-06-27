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
import org.activiti.api.model.shared.event.VariableDeletedEvent;
import org.activiti.api.model.shared.event.VariableEvent.VariableEvents;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.activiti.engine.impl.variable.BooleanType;
import org.activiti.engine.impl.variable.VariableType;
import org.junit.jupiter.api.Test;

class ToVariableDeletedConverterTest {

    private ToVariableDeletedConverter converter = new ToVariableDeletedConverter();

    @Test
    void should_convertToVariableDeletedEvent() {
        ActivitiVariableEventImpl internalEvent = new ActivitiVariableEventImpl(ActivitiEventType.VARIABLE_DELETED);
        internalEvent.setVariableName("variableName");
        internalEvent.setProcessInstanceId("processInstanceId");
        internalEvent.setTaskId("taskId");
        VariableType variableType = new BooleanType();
        internalEvent.setVariableType(variableType);
        Object value = mock(Object.class);
        internalEvent.setVariableValue(value);

        Optional<VariableDeletedEvent> result = converter.from(internalEvent);

        assertThat(result).isPresent();
        VariableDeletedEvent actualEvent = result.get();
        assertThat(actualEvent.getEventType()).isEqualTo(VariableEvents.VARIABLE_DELETED);
        VariableInstance actualEntity = actualEvent.getEntity();
        assertThat(actualEntity.getName()).isEqualTo("variableName");
        assertThat(actualEntity.getProcessInstanceId()).isEqualTo("processInstanceId");
        assertThat(actualEntity.getTaskId()).isEqualTo("taskId");
        assertThat(actualEntity.getType()).isEqualTo("boolean");
        Object actualValue = actualEntity.getValue();
        assertThat(actualValue).isSameAs(value);

    }
}
