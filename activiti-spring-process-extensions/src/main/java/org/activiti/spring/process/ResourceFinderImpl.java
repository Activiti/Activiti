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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ResourceFinderImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFinderImpl.class);
    
    private ResourcePatternResolver resourceLoader;
    private ObjectMapper objectMapper;
    
    public ResourceFinderImpl(ResourcePatternResolver resourceLoader,
                              ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;   
        this.objectMapper = objectMapper;  
    }
    
    public List<Resource> discoverResources(ResourceFinderDescriptor resourceFinderDescriptor) throws IOException {
        List<Resource> resources = new ArrayList<>();
                
        if (resourceFinderDescriptor.isCheckResources()) {
            for (String suffix : resourceFinderDescriptor.getLocationSuffixes()) {
                String path = resourceFinderDescriptor.getLocationPrefix() + suffix;
                resources.addAll(Arrays.asList(resourceLoader.getResources(path)));
            }
            if (resources.isEmpty()) {
                LOGGER.info(resourceFinderDescriptor.getMsgForEmptyResources() + " `" + resourceFinderDescriptor.getLocationPrefix() + "`");
            } else {
                
                List<String> resourcesNames = resources.stream().map(Resource::getFilename).collect(Collectors.toList());
                LOGGER.info(resourceFinderDescriptor.getMsgForResourcesLoadOk() + " " + resourcesNames);
            }
        }
        return resources;
    }
 
    public ProcessExtensionModel read(InputStream inputStream) throws IOException {
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        return  objectMapper.readValue(inputStream,
                ProcessExtensionModel.class);
    }
    
    public Map<String, Resource> readMapProcessExtensions(ResourceFinderDescriptor resourceFinderDescriptor) throws IOException {
        Map<String, Resource> mapProcessExtensionResources = new HashMap<>();
        
        List<Resource> procExtensionResources = discoverResources(resourceFinderDescriptor);
        
        if (!procExtensionResources.isEmpty()) {
            for (Resource resource : procExtensionResources) {
                ProcessExtensionModel processExtensionModel = read(resource.getInputStream());
                
                if (processExtensionModel != null) {
                    mapProcessExtensionResources.put(processExtensionModel.getId(),resource);
                }
            }
        }
        return mapProcessExtensionResources;
    }
    

  
}
