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

import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.runtime.api.model.impl.APITaskConverter;

import java.util.Optional;

public class ToAPITaskAssignedEventConverter implements EventConverter<TaskAssignedEvent, ActivitiEntityEvent> {

    private final APITaskConverter taskConverter;

    public ToAPITaskAssignedEventConverter(APITaskConverter taskConverter) {
        this.taskConverter = taskConverter;
    }

    @Override
    public Optional<TaskAssignedEvent> from(ActivitiEntityEvent internalEvent) {
        return Optional.of(new TaskAssignedEventImpl(taskConverter.from((org.activiti.engine.task.Task) internalEvent.getEntity())));
    }
}
