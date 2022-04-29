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
package org.activiti.runtime.api.model.impl;

import java.util.Objects;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.runtime.model.impl.ProcessDefinitionImpl;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;

public class APIProcessDefinitionConverter extends ListConverter<org.activiti.engine.repository.ProcessDefinition, ProcessDefinition>
        implements ModelConverter<org.activiti.engine.repository.ProcessDefinition, ProcessDefinition> {

    private RepositoryService repositoryService;

    public APIProcessDefinitionConverter(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public ProcessDefinition from(org.activiti.engine.repository.ProcessDefinition internalProcessDefinition) {
        ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl();
        processDefinition.setId(internalProcessDefinition.getId());
        processDefinition.setName(internalProcessDefinition.getName());
        processDefinition.setDescription(internalProcessDefinition.getDescription());
        processDefinition.setVersion(internalProcessDefinition.getVersion());
        processDefinition.setKey(internalProcessDefinition.getKey());
        processDefinition.setAppVersion(Objects.toString(internalProcessDefinition.getAppVersion(), null));
        processDefinition.setCategory(internalProcessDefinition.getCategory());
        BpmnModel model = repositoryService.getBpmnModel(internalProcessDefinition.getId());
        processDefinition.setFormKey(model.getStartFormKey(internalProcessDefinition.getKey()));
        return processDefinition;
    }
}
