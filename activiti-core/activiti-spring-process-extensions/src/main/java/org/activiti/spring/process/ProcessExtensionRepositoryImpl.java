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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.resources.DeploymentResourceLoader;
import org.springframework.lang.NonNull;

public class ProcessExtensionRepositoryImpl implements ProcessExtensionRepository {

    private final DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader;
    private final ProcessExtensionResourceReader processExtensionReader;
    private final RepositoryService repositoryService;

    public ProcessExtensionRepositoryImpl(DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader,
        ProcessExtensionResourceReader processExtensionReader, RepositoryService repositoryService) {

        this.processExtensionLoader = processExtensionLoader;
        this.processExtensionReader = processExtensionReader;
        this.repositoryService = repositoryService;
    }

    @Override
    public Optional<Extension> getExtensionsForId(@NonNull String processDefinitionId) {
        return Optional.of(processDefinitionId)
            .map(repositoryService::getProcessDefinition)
            .map(this::getExtensionsFor);
    }

    private Extension getExtensionsFor(ProcessDefinition processDefinition) {
        Map<String, Extension> processExtensionModelMap = getProcessExtensionsForDeploymentId(processDefinition.getDeploymentId());

        return processExtensionModelMap.get(processDefinition.getKey());
    }

    private Map<String, Extension> getProcessExtensionsForDeploymentId(String deploymentId) {
        List<ProcessExtensionModel> processExtensionModels = processExtensionLoader.loadResourcesForDeployment(deploymentId,
                processExtensionReader);

        return processExtensionModels
            .stream()
            .flatMap(it -> it.getAllExtensions().entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
