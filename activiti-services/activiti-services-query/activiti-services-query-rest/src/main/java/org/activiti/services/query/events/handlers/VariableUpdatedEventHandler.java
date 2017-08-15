/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.query.events.handlers;

import java.util.Date;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.model.Variable;
import org.activiti.services.query.events.VariableUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableUpdatedEventHandler implements QueryEventHandler {

    private ProcessVariableUpdateHandler processVariableUpdateHandler;

    private TaskVariableUpdatedHandler taskVariableUpdatedHandler;

    @Autowired
    public VariableUpdatedEventHandler(ProcessVariableUpdateHandler processVariableUpdateHandler,
                                       TaskVariableUpdatedHandler taskVariableUpdatedHandler) {
        this.processVariableUpdateHandler = processVariableUpdateHandler;
        this.taskVariableUpdatedHandler = taskVariableUpdatedHandler;
    }

    @Override
    public void handle(ProcessEngineEvent event) {
        VariableUpdatedEvent variableUpdatedEvent = (VariableUpdatedEvent) event;
        Variable variable = new Variable(variableUpdatedEvent.getVariableType(),
                                         variableUpdatedEvent.getVariableName(),
                                         variableUpdatedEvent.getProcessInstanceId(),
                                         variableUpdatedEvent.getTaskId(),
                                         new Date(variableUpdatedEvent.getTimestamp()),
                                         new Date(variableUpdatedEvent.getTimestamp()),
                                         variableUpdatedEvent.getExecutionId(),
                                         variableUpdatedEvent.getVariableValue());
        if (variableUpdatedEvent.getTaskId() != null) {
            taskVariableUpdatedHandler.handle(variable);
        } else {
            processVariableUpdateHandler.handle(variable);
        }

    }

    @Override
    public Class<? extends ProcessEngineEvent> getHandledEventClass() {
        return VariableUpdatedEvent.class;
    }
}
