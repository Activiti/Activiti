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

package org.activiti.runtime.api.connector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.VariableDefinition;
import org.activiti.engine.delegate.DelegateExecution;

public class InboundVariablesProvider {

    private InboundVariableValueProvider mappedValueProvider;

    public InboundVariablesProvider(InboundVariableValueProvider mappedValueProvider) {
        this.mappedValueProvider = mappedValueProvider;
    }

    public Map<String, Object> calculateVariables(DelegateExecution execution,
                                                  ActionDefinition actionDefinition) {
        Map<String, Object> inboundVariables;
        if (actionDefinition == null) {
            inboundVariables = execution.getVariables();
        } else {
            List<VariableDefinition> connectorInputs = actionDefinition.getInputs();
            inboundVariables = new HashMap<>();
            connectorInputs.forEach(variableDefinition -> {
                Object value = mappedValueProvider.calculateMappedValue(variableDefinition,
                                                                        execution);
                if (value != null) {
                    inboundVariables.put(variableDefinition.getName(),
                                         value);
                }
            });
        }
        return inboundVariables;
    }
}
