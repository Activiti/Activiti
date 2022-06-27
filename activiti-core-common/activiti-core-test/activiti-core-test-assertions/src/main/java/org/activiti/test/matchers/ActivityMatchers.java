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
package org.activiti.test.matchers;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public abstract class ActivityMatchers {

    private String definitionKey;

    protected ActivityMatchers(String definitionKey) {
        this.definitionKey = definitionKey;
    }

    public abstract String getActivityType();

    public OperationScopeMatcher hasBeenStarted() {
        return (operationScope, events) -> {
            List<BPMNActivityStartedEvent> startedEvents = events
                    .stream()
                    .filter(event -> BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED.equals(event.getEventType()))
                    .map(BPMNActivityStartedEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(startedEvents)
                    .filteredOn(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .extracting(event -> event.getEntity().getActivityType(),
                                event -> event.getEntity().getElementId())
                    .contains(tuple(getActivityType(),
                                    definitionKey));
        };
    }

    public OperationScopeMatcher hasBeenCompleted() {

        return (operationScope, events) -> {
            hasBeenStarted().match(operationScope,
                                   events);
            List<BPMNActivityCompletedEvent> completedEvents = events
                    .stream()
                    .filter(event -> BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED.equals(event.getEventType()))
                    .map(BPMNActivityCompletedEvent.class::cast)
                    .collect(Collectors.toList());

            assertThat(completedEvents)
                    .filteredOn(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .extracting(event -> event.getEntity().getActivityType(),
                                event -> event.getEntity().getElementId())
                    .as("Unable to find event " + BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED + " for element " + definitionKey)
                    .contains(tuple(getActivityType(),
                                    definitionKey));
        };
    }
}
