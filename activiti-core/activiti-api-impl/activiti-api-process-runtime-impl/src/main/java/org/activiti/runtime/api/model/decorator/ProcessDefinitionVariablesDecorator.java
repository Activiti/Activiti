/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.runtime.api.model.decorator;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.runtime.model.impl.VariableDefinitionImpl;
import org.activiti.spring.process.CachingProcessExtensionService;
import org.activiti.spring.process.model.VariableDefinition;

import java.util.Map;
import java.util.stream.Collectors;

public class ProcessDefinitionVariablesDecorator implements ProcessDefinitionDecorator {

    private static final String HANDLED_VALUE = "variables";

    private final CachingProcessExtensionService processExtensionService;

    public ProcessDefinitionVariablesDecorator(CachingProcessExtensionService processExtensionService) {
        this.processExtensionService = processExtensionService;
    }

    @Override
    public String getHandledValue() {
        return HANDLED_VALUE;
    }

    @Override
    public ProcessDefinition decorate(ProcessDefinition processDefinition) {
        Map<String, VariableDefinition> variables =
            processExtensionService.getExtensionsForId(processDefinition.getId()).getProperties();
        processDefinition.getVariableDefinitions().addAll(variables.values().stream()
            .filter(variableDefinition -> Boolean.TRUE.equals(variableDefinition.getDisplay()))
            .map(this::convert)
            .collect(Collectors.toList()));
        return processDefinition;
    }

    private org.activiti.api.process.model.VariableDefinition convert(VariableDefinition variableDefinition) {
        VariableDefinitionImpl variableDefinitionImpl = new VariableDefinitionImpl();
        variableDefinitionImpl.setId(variableDefinition.getId());
        variableDefinitionImpl.setName(variableDefinition.getName());
        variableDefinitionImpl.setDescription(variableDefinition.getDescription());
        variableDefinitionImpl.setType(variableDefinition.getType());
        variableDefinitionImpl.setRequired(variableDefinition.isRequired());
        variableDefinitionImpl.setDisplay(variableDefinition.getDisplay());
        variableDefinitionImpl.setDisplayName(variableDefinition.getDisplayName());
        return variableDefinitionImpl;
    }
}
