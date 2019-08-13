/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.runtime.events.ProcessResumedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;

import java.util.Optional;

import static org.activiti.runtime.api.event.impl.ActivitiEntityEventHelper.isProcessInstanceEntity;

public class ToProcessResumedConverter implements EventConverter<ProcessResumedEvent, ActivitiEntityEvent> {

    private final APIProcessInstanceConverter processInstanceConverter;

    public ToProcessResumedConverter(APIProcessInstanceConverter processInstanceConverter) {
        this.processInstanceConverter = processInstanceConverter;
    }

    @Override
    public Optional<ProcessResumedEvent> from(ActivitiEntityEvent internalEvent) {
        Object entity = internalEvent.getEntity();

        ProcessResumedEventImpl event = null;
        if (isProcessInstanceEntity(entity)) {
            event = new ProcessResumedEventImpl(processInstanceConverter.from(((ExecutionEntity)
                    entity).getProcessInstance()));
        }
        return Optional.ofNullable(event);
    }

}
