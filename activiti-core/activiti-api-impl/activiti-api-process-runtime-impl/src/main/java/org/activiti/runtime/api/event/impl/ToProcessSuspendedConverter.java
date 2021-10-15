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

import static org.activiti.runtime.api.event.impl.ActivitiEntityEventHelper.isProcessInstanceEntity;

import java.util.Optional;
import org.activiti.api.process.runtime.events.ProcessSuspendedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;

public class ToProcessSuspendedConverter
    implements EventConverter<ProcessSuspendedEvent, ActivitiEntityEvent> {

    private final APIProcessInstanceConverter processInstanceConverter;

    public ToProcessSuspendedConverter(
        APIProcessInstanceConverter processInstanceConverter
    ) {
        this.processInstanceConverter = processInstanceConverter;
    }

    @Override
    public Optional<ProcessSuspendedEvent> from(
        ActivitiEntityEvent internalEvent
    ) {
        ProcessSuspendedEvent event = null;
        if (isProcessInstanceEntity(internalEvent.getEntity())) {
            event =
                new ProcessSuspendedEventImpl(
                    processInstanceConverter.from(
                        (
                            (ExecutionEntity) internalEvent.getEntity()
                        ).getProcessInstance()
                    )
                );
        }
        return Optional.ofNullable(event);
    }
}
