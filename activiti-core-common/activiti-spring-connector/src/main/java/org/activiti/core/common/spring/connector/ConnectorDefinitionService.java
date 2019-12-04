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

package org.activiti.core.common.spring.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ConnectorDefinitionService {

    private String connectorRoot;

    private final ObjectMapper objectMapper;

    private ResourcePatternResolver resourceLoader;

    public ConnectorDefinitionService(String connectorRoot, ObjectMapper objectMapper, ResourcePatternResolver resourceLoader) {
        this.connectorRoot = connectorRoot;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    private Optional<Resource[]> retrieveResources() throws IOException {

        Optional<Resource[]> resources = Optional.empty();

        Resource connectorRootPath = resourceLoader.getResource(connectorRoot);
        if (connectorRootPath.exists()) {
            return Optional.ofNullable(resourceLoader.getResources(connectorRoot + "**.json"));
        }
        return resources;
    }

    private ConnectorDefinition read(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream,
                ConnectorDefinition.class);
    }

    public List<ConnectorDefinition> get() throws IOException {

        List<ConnectorDefinition> connectorDefinitions = new ArrayList<>();
        Optional<Resource[]> resourcesOptional = retrieveResources();
        if (resourcesOptional.isPresent()) {
            for (Resource resource : resourcesOptional.get()) {
                connectorDefinitions.add(read(resource.getInputStream()));
            }
            validate(connectorDefinitions);
        }
        return connectorDefinitions;
    }

    protected void validate(List<ConnectorDefinition> connectorDefinitions) {
        if (!connectorDefinitions.isEmpty()) {
            Set<String> processedNames = new HashSet<>();

            for (ConnectorDefinition connectorDefinition : connectorDefinitions) {
                String name = connectorDefinition.getName();
                if (name == null || name.isEmpty()) {
                    throw new IllegalStateException("connectorDefinition name cannot be null or empty");
                }
                if (name.contains(".")) {
                    throw new IllegalStateException("connectorDefinition name cannot have '.' character");
                }
                if (!processedNames.add(name)) {
                    throw new IllegalStateException("More than one connectorDefinition with name '" + name + "' was found. Names must be unique.");
                }
            }
        }
    }
}

