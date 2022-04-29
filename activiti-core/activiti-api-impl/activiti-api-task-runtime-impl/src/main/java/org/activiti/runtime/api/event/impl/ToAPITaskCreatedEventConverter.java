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

import java.util.Optional;

import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.runtime.api.model.impl.APITaskConverter;

public class ToAPITaskCreatedEventConverter implements EventConverter<TaskCreatedEvent, ActivitiEntityEvent> {

    private final APITaskConverter taskConverter;

    public ToAPITaskCreatedEventConverter(APITaskConverter taskConverter) {
        this.taskConverter = taskConverter;
    }

    @Override
    public Optional<TaskCreatedEvent> from(ActivitiEntityEvent internalEvent) {
        return Optional.of(new TaskCreatedEventImpl(taskConverter.fromWithCandidates((org.activiti.engine.task.Task) internalEvent.getEntity())));
    }
}
