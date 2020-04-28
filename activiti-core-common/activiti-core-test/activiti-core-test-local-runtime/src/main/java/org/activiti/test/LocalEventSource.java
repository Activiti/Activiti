/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.test;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.task.model.events.TaskRuntimeEvent;

public class LocalEventSource implements EventSource {

    private List<RuntimeEvent<?, ?>> collectedEvents = new ArrayList<>();

    public void addCollectedEvents(RuntimeEvent<?, ?> event) {
        this.collectedEvents.add(event);
    }

    @Override
    public List<RuntimeEvent<?, ?>> getEvents() {
        return unmodifiableList(collectedEvents);
    }

    public void clearEvents() {
        collectedEvents.clear();
    }

    public <T extends RuntimeEvent<?,?>> List<T> getEvents(Class<T> eventType) {
        return collectedEvents
                .stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .collect(collectingAndThen(Collectors.toList(),
                                           Collections::unmodifiableList));
    }

    public List<RuntimeEvent<?, ?>> getEvents(Enum<?> ... eventTypes) {
        return collectedEvents
                .stream()
                .filter(event -> asList(eventTypes).contains(event.getEventType()))
                .collect(collectingAndThen(Collectors.toList(),
                                           Collections::unmodifiableList));

    }

    public List<RuntimeEvent<?, ?>> getTaskEvents() {
        return getEvents(TaskRuntimeEvent.TaskEvents.values());
    }

    public List<RuntimeEvent<?, ?>> getProcessInstanceEvents() {
        return getEvents(ProcessRuntimeEvent.ProcessEvents.values());
    }

}
