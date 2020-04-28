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

package org.activiti.application;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ApplicationDiscovery {

    private ResourcePatternResolver resourceLoader;
    private String applicationsLocation;

    public ApplicationDiscovery(ResourcePatternResolver resourceLoader,
                                String applicationsLocation) {
        this.resourceLoader = resourceLoader;
        this.applicationsLocation = applicationsLocation;
    }

    public List<Resource> discoverApplications() {
        List<Resource> resources = new ArrayList<>();
        Resource resource = resourceLoader.getResource(applicationsLocation);
        if (resource.exists()) {
            try {
                resources = asList(resourceLoader.getResources(applicationsLocation + "**.zip"));
            } catch (IOException e) {
                throw new ApplicationLoadException("Unable to load application resources", e);
            }
        }
        return resources;
    }
}
