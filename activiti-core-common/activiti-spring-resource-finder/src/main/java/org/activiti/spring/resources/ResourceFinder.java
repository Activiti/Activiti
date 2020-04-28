/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.spring.resources;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ResourceFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFinder.class);

    private ResourcePatternResolver resourceLoader;

    public ResourceFinder(ResourcePatternResolver resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<Resource> discoverResources(ResourceFinderDescriptor resourceFinderDescriptor) throws IOException {
        List<Resource> resources = new ArrayList<>();

        if (resourceFinderDescriptor.shouldLookUpResources()) {
            for (String suffix : resourceFinderDescriptor.getLocationSuffixes()) {
                String path = resourceFinderDescriptor.getLocationPrefix() + suffix;
                resources.addAll(asList(resourceLoader.getResources(path)));
            }
            if (resources.isEmpty()) {
                LOGGER.info(resourceFinderDescriptor.getMsgForEmptyResources());
            } else {
                resourceFinderDescriptor.validate(resources);

                List<String> foundResources = resources.stream().map(Resource::getFilename).collect(Collectors.toList());
                LOGGER.info(resourceFinderDescriptor.getMsgForResourcesFound(foundResources));
            }
        }
        return resources;
    }


}
