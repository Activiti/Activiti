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

package org.activiti.spring.boot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ProcessDefinitionResourceFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionResourceFinder.class);

    private ActivitiProperties activitiProperties;

    private ResourcePatternResolver resourceLoader;

    public ProcessDefinitionResourceFinder(ActivitiProperties activitiProperties,
                                           ResourcePatternResolver resourceLoader) {
        this.activitiProperties = activitiProperties;
        this.resourceLoader = resourceLoader;
    }

    public List<Resource> discoverProcessDefinitionResources() throws IOException {
        List<Resource> resources = new ArrayList<>();
        if (activitiProperties.isCheckProcessDefinitions()) {
            for (String suffix : activitiProperties.getProcessDefinitionLocationSuffixes()) {
                String path = activitiProperties.getProcessDefinitionLocationPrefix() + suffix;
                resources.addAll(Arrays.asList(resourceLoader.getResources(path)));
            }
            if (resources.isEmpty()) {
                LOGGER.info("No process definitions were found for auto-deployment in the location `" + activitiProperties.getProcessDefinitionLocationPrefix() + "`");
            } else {
                List<String> resourcesNames = resources.stream().map(Resource::getFilename).collect(Collectors.toList());
                LOGGER.info("The following process definition files will be deployed: " + resourcesNames);
            }
        }
        return resources;
    }
}
