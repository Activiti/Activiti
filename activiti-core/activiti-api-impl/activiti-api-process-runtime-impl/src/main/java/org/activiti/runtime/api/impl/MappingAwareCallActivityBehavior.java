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

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.bpmn.behavior.VariablesPropagator;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.ProcessVariablesInitiator;

public class MappingAwareCallActivityBehavior extends CallActivityBehavior {

    private VariablesCalculator variablesCalculator;
    private ProcessVariablesInitiator processVariablesInitiator;

    public MappingAwareCallActivityBehavior(String processDefinitionKey, List<MapExceptionEntry> mapExceptions,
        VariablesCalculator variablesCalculator, ProcessVariablesInitiator processVariablesInitiator, VariablesPropagator variablesPropagator) {
        super(processDefinitionKey, mapExceptions, variablesPropagator);
        this.variablesCalculator = variablesCalculator;
        this.processVariablesInitiator = processVariablesInitiator;
    }

    public MappingAwareCallActivityBehavior(Expression processDefinitionExpression, List<MapExceptionEntry> mapExceptions,
        VariablesCalculator variablesCalculator, ProcessVariablesInitiator processVariablesInitiator, VariablesPropagator variablesPropagator) {
        super(processDefinitionExpression, mapExceptions, variablesPropagator);
        this.variablesCalculator = variablesCalculator;
        this.processVariablesInitiator = processVariablesInitiator;
    }

    @Override
    protected Map<String, Object> calculateInboundVariables(DelegateExecution execution,
                                                            ProcessDefinition processDefinition) {

        Map<String, Object> inputVariables = variablesCalculator.calculateInputVariables(execution);
        return processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition, inputVariables);
    }

}
