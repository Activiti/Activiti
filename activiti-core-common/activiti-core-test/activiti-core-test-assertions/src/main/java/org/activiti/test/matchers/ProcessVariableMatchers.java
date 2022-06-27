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

import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ProcessVariableMatchers {

    private String variableName;
    private Object value;

    private ProcessVariableMatchers(String variableName,
                                    Object value) {

        this.variableName = variableName;
        this.value = value;
    }

    public static ProcessVariableMatchers processVariable(String variableName, Object value) {
        return new ProcessVariableMatchers(variableName, value);
    }

    public OperationScopeMatcher hasBeenCreated() {
        return (operationScope, events) -> {
            List<VariableCreatedEvent> variableCreatedEvents = events
                    .stream()
                    .filter(event -> VariableEvent.VariableEvents.VARIABLE_CREATED.equals(event.getEventType()))
                    .map(VariableCreatedEvent.class::cast)
                    .filter(event -> !event.getEntity().isTaskVariable())
                    .filter(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .collect(Collectors.toList());
            assertThat(variableCreatedEvents)
                    .extracting(event -> event.getEntity().getName(),
                                event -> event.getEntity().getValue())
                    .as("Unable to find event " + VariableEvent.VariableEvents.VARIABLE_CREATED + " for variable "
                                + variableName + " in process instance " + operationScope.getProcessInstanceId())
                    .contains(tuple(variableName,
                                    value));
        };
    }
}
