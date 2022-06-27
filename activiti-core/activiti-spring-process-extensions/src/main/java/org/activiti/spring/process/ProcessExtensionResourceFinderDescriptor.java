/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.util.ArrayList;
import java.util.List;

import org.activiti.spring.resources.ResourceFinderDescriptor;
import org.springframework.core.io.Resource;

public class ProcessExtensionResourceFinderDescriptor implements ResourceFinderDescriptor {

    private boolean checkResources;
    private String locationPrefix;
    private List<String> locationSuffixes;

    public ProcessExtensionResourceFinderDescriptor(boolean checkResources,
                                                    String locationPrefix,
                                                    String locationSuffix) {

        this.checkResources = checkResources;
        this.locationPrefix = locationPrefix;
        locationSuffixes = new ArrayList<>();
        locationSuffixes.add(locationSuffix);
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
        return "No process extensions were found for auto-deployment in the location '" + locationPrefix + "'";
    }

    @Override
    public String getMsgForResourcesFound(List<String> processExtensionFiles) {
        return "The following process extension files will be deployed: " + processExtensionFiles;
    }

    @Override
    public void validate(List<Resource> resources) {

    }

}
