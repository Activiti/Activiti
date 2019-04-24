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

package org.activiti.spring.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.springframework.core.io.Resource;

public class ProcessConnectorResourceFinderDescriptor implements ResourceFinderDescriptor {

    private boolean checkResources;
    private String locationPrefix;
    private List<String> locationSuffixes;
    private final ObjectMapper objectMapper;
        
    public ProcessConnectorResourceFinderDescriptor(boolean checkResources,
                                          String locationPrefix,
                                          String locationSuffix,
                                          ObjectMapper objectMapper) {
        
        this.checkResources = checkResources;
        this.locationPrefix = locationPrefix;
        locationSuffixes = new ArrayList<>();
        if (locationSuffix != null) {
            locationSuffixes.add(locationSuffix);
        }
        this.objectMapper = objectMapper;
    }
    
    @Override
    public List<String> getLocationSuffixes() {
        return locationSuffixes;
    }

    @Override
    public String getLocationPrefix() {
        return locationPrefix;
    }

    @Override
    public boolean shouldLookUpResources() {
        return checkResources;
    }

    @Override
    public String getMsgForEmptyResources() {
        return "No process connectors were found for auto-deployment in the location";
    }

    @Override
    public String getMsgForResourcesFound() {
        return "The following process connector files will be deployed:";
    }
    
    private ConnectorDefinition read(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream,
                ConnectorDefinition.class);
    }
    
    @Override
    public void validate(List<Resource> resources) throws IOException {
        if (!resources.isEmpty()) {
      
            List<ConnectorDefinition> connectorDefinitions = new ArrayList<>();
            
            for (Resource resource : resources) {
                connectorDefinitions.add(read(resource.getInputStream()));
            }
            
            if (!connectorDefinitions.isEmpty()) {
                Set<String> duplicates = new HashSet<>(); 
                String name;

                for (ConnectorDefinition connectorDefinition : connectorDefinitions) {
                    name = connectorDefinition.getName();
                    if (name == null || name.isEmpty()) {
                        throw new IllegalStateException("connectorDefinition name cannot be empty");
                        
                    }
                    if (name.contains(".")) {
                        throw new IllegalStateException("connectorDefinition name cannot have '.' character");
                    }
                    if (!duplicates.add(name)) {
                        throw new IllegalStateException("connectorDefinition name '" + name + "' already present");
                    }
                
                }
            }

        }
        
    }
    
    
    
}
