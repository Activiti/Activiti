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

package org.activiti.runtime.api.model.impl;

import org.activiti.runtime.api.model.ProcessDefinition;

public class APIProcessDefinitionConverter extends ListConverter<org.activiti.engine.repository.ProcessDefinition, ProcessDefinition>
        implements ModelConverter<org.activiti.engine.repository.ProcessDefinition, ProcessDefinition> {

    public ProcessDefinition from(org.activiti.engine.repository.ProcessDefinition internalProcessDefinition) {
        ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl();
        processDefinition.setId(internalProcessDefinition.getId());
        processDefinition.setName(internalProcessDefinition.getName());
        processDefinition.setDescription(internalProcessDefinition.getDescription());
        processDefinition.setVersion(internalProcessDefinition.getVersion());
        processDefinition.setKey(internalProcessDefinition.getKey());
        return processDefinition;
    }
}
