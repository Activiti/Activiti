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


package org.activiti.spring.process;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.VariableDefinition;
import org.springframework.lang.NonNull;

import java.util.Optional;

public class ProcessExtensionService {

    private final static Extension EMPTY_EXTENSION = new Extension();

    private final ProcessExtensionRepository processExtensionRepository;

    public ProcessExtensionService(ProcessExtensionRepository processExtensionRepository) {
        this.processExtensionRepository = processExtensionRepository;
    }

    public boolean hasExtensionsFor(@NonNull ProcessDefinition processDefinition) {
        return hasExtensionsFor(processDefinition.getId());
    }

    public boolean hasExtensionsFor(@NonNull String processDefinitionId) {
        return processExtensionRepository.getExtensionsForId(processDefinitionId).isPresent();
    }

    public Extension getExtensionsFor(@NonNull ProcessDefinition processDefinition) {
        return getExtensionsForId(processDefinition.getId());
    }

    public Extension getExtensionsForId(@NonNull String processDefinitionId) {
        return processExtensionRepository.getExtensionsForId(processDefinitionId).orElse(EMPTY_EXTENSION);
    }
    public boolean hasEphemeralVariable(@NonNull String processDefinitionId,
                                        @NonNull String variableName) {
        Extension extension = this.getExtensionsForId(processDefinitionId);
        return Optional.ofNullable(extension)
            .map(ext -> ext.getPropertyByName(variableName))
            .map(VariableDefinition::isEphemeral)
            .orElse(false);

    }
}
