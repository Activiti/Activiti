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

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.events.VariableDeletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableDeletedEventHandler implements QueryEventHandler {

    private final ProcessVariableDeletedHandler processVariableDeletedHandler;

    private final TaskVariableDeletedHandler taskVariableDeletedHandler;

    @Autowired
    public VariableDeletedEventHandler(ProcessVariableDeletedHandler processVariableDeletedHandler,
                                       TaskVariableDeletedHandler taskVariableDeletedHandler) {
        this.processVariableDeletedHandler = processVariableDeletedHandler;
        this.taskVariableDeletedHandler = taskVariableDeletedHandler;
    }

    @Override
    public void handle(ProcessEngineEvent event) {
        VariableDeletedEvent variableDeletedEvent = (VariableDeletedEvent) event;
        if (variableDeletedEvent.getTaskId() == null) {
            processVariableDeletedHandler.handle(variableDeletedEvent);
        } else {
            taskVariableDeletedHandler.handle(variableDeletedEvent);
        }

    }

    @Override
    public Class<? extends ProcessEngineEvent> getHandledEventClass() {
        return VariableDeletedEvent.class;
    }
}
