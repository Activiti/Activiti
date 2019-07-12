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

package org.activiti.runtime.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;

public class DefaultCallActivityBehavior extends CallActivityBehavior {

    private VariablesMappingProvider mappingProvider;

    public DefaultCallActivityBehavior(String processDefinitionKey, List<MapExceptionEntry> mapExceptions, VariablesMappingProvider mappingProvider) {
        super(processDefinitionKey, mapExceptions);
        this.mappingProvider = mappingProvider;
    }

    public DefaultCallActivityBehavior(Expression processDefinitionExpression, List<MapExceptionEntry> mapExceptions, VariablesMappingProvider mappingProvider) {
        super(processDefinitionExpression, mapExceptions);
        this.mappingProvider = mappingProvider;
    }


    @Override
    protected Map<String, Object> getInboundVariables(DelegateExecution execution) {
        return mappingProvider.calculateInputVariables(execution);
    }

    @Override
    protected Map<String, Object> getOutBoundVariables(CommandContext commandContext,
                                                       DelegateExecution execution,
                                                       Map<String, Object> taskCompleteVariables) {

        return mappingProvider.calculateOutPutVariables(execution,
                                                        taskCompleteVariables);
    }
    @Override
    protected Map<String, Object> getVariablesFromExtensionFile(ProcessDefinition processDefinition) {
        Map<String, Object> extensionFileVariables=mappingProvider.getProcessVariablesInitiator().getVariablesFromExtensionFile(processDefinition,new HashMap<>() );
        return extensionFileVariables;
    }

}
