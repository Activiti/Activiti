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
package org.activiti.runtime.api.impl;

import java.util.Map;
import java.util.Optional;

import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.el.ExpressionManager;

public class JsonMessagePayloadMappingProvider implements MessagePayloadMappingProvider {

    private final Event bpmnEvent;
    private final MessageEventDefinition messageEventDefinition;
    private final ExpressionManager expressionManager;
    private final VariablesCalculator variablesCalculator;

    public JsonMessagePayloadMappingProvider(Event bpmnEvent,
                                             MessageEventDefinition messageEventDefinition,
                                             ExpressionManager expressionManager,
                                             VariablesCalculator variablesCalculator) {
        this.bpmnEvent = bpmnEvent;
        this.messageEventDefinition = messageEventDefinition;
        this.expressionManager = expressionManager;
        this.variablesCalculator = variablesCalculator;
    }

    public Optional<Map<String, Object>> getMessagePayload(DelegateExecution execution) {
        return Optional.of(variablesCalculator.calculateInputVariables(execution))
                       .filter(payload -> !payload.isEmpty());
    }

    public Event getBpmnEvent() {
        return bpmnEvent;
    }

    public MessageEventDefinition getMessageEventDefinition() {
        return messageEventDefinition;
    }

    public ExpressionManager getExpressionManager() {
        return expressionManager;
    }

    public VariablesCalculator getVariablesCalculator() {
        return variablesCalculator;
    }
}
