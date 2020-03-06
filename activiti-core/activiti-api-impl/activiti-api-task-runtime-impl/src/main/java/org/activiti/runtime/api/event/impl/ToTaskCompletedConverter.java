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

import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.Task;
import org.activiti.runtime.api.model.impl.APITaskConverter;

import java.util.Optional;

public class ToTaskCompletedConverter implements EventConverter<TaskCompletedEvent, ActivitiEntityEvent> {

    private APITaskConverter converter;

    public ToTaskCompletedConverter(APITaskConverter converter) {
        this.converter = converter;
    }

    @Override
    public Optional<TaskCompletedEvent> from(ActivitiEntityEvent internalEvent) {
        return Optional.of(new TaskCompletedImpl(converter.from((Task) internalEvent.getEntity(), org.activiti.api.task.model.Task.TaskStatus.COMPLETED)));
    }
}
