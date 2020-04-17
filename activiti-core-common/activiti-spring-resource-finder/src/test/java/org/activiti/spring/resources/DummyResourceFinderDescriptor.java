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
import java.util.List;

import org.springframework.core.io.Resource;

public class DummyResourceFinderDescriptor implements ResourceFinderDescriptor {

    private List<String> suffixes;

    private String locationPrefix;

    public DummyResourceFinderDescriptor(String locationPrefix,
                                         String ... suffixes) {
        this.suffixes = asList(suffixes);
        this.locationPrefix = locationPrefix;
    }

    @Override
    public List<String> getLocationSuffixes() {
        return suffixes;
    }

    @Override
    public String getLocationPrefix() {
        return locationPrefix;
    }

    @Override
    public boolean shouldLookUpResources() {
        return true;
    }

    @Override
    public void validate(List<Resource> resources) throws IOException {

    }

    @Override
    public String getMsgForEmptyResources() {
        return "No resources found";
    }

    @Override
    public String getMsgForResourcesFound(List<String> foundResources) {
        return "Found resources: " + foundResources;
    }
}
