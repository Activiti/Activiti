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

import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProviderFactory;
import org.activiti.engine.impl.el.ExpressionManager;

public class JsonMessagePayloadMappingProviderFactory implements MessagePayloadMappingProviderFactory {

    private final VariablesCalculator variablesCalculator;

    public JsonMessagePayloadMappingProviderFactory(
        VariablesCalculator variablesCalculator) {
        this.variablesCalculator = variablesCalculator;
    }

    @Override
    public MessagePayloadMappingProvider create(Event bpmnEvent,
                                                MessageEventDefinition messageEventDefinition,
                                                ExpressionManager expressionManager) {
        return new JsonMessagePayloadMappingProvider(bpmnEvent,
                                                     messageEventDefinition,
                                                     expressionManager,
            variablesCalculator);
    }

}
