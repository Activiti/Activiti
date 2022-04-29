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

import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.runtime.event.impl.BPMNActivityStartedEventImpl;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.runtime.api.model.impl.ToActivityConverter;

import java.util.Optional;

public class ToActivityStartedConverter implements EventConverter<BPMNActivityStartedEvent, ActivitiActivityEvent> {

    private ToActivityConverter toActivityConverter;

    public ToActivityStartedConverter(ToActivityConverter toActivityConverter) {
        this.toActivityConverter = toActivityConverter;
    }

    @Override
    public Optional<BPMNActivityStartedEvent> from(ActivitiActivityEvent internalEvent) {
        return Optional.of(new BPMNActivityStartedEventImpl(toActivityConverter.from(internalEvent)));
    }
}
