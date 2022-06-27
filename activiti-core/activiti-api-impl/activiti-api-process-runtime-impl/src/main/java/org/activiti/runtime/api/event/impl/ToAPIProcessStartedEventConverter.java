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

import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;

import java.util.Optional;

public class ToAPIProcessStartedEventConverter implements EventConverter<ProcessStartedEvent, ActivitiProcessStartedEvent> {

    private final APIProcessInstanceConverter processInstanceConverter;

    public ToAPIProcessStartedEventConverter(APIProcessInstanceConverter processInstanceConverter) {
        this.processInstanceConverter = processInstanceConverter;
    }

    @Override
    public Optional<ProcessStartedEvent> from(ActivitiProcessStartedEvent internalEvent) {
        ExecutionEntity entity = (ExecutionEntity)
                internalEvent.getEntity();
        ProcessStartedEventImpl processStartedEvent = new ProcessStartedEventImpl(
                processInstanceConverter.from(entity.getProcessInstance()));
        processStartedEvent.setNestedProcessDefinitionId(internalEvent.getNestedProcessDefinitionId());
        processStartedEvent.setNestedProcessInstanceId(internalEvent.getNestedProcessInstanceId());
        return Optional.of(processStartedEvent);
    }
}
