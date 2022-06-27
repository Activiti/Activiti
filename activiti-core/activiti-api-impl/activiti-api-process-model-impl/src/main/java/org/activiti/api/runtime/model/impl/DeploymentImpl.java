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
package org.activiti.api.runtime.model.impl;

import org.activiti.api.process.model.Deployment;

public class DeploymentImpl implements Deployment {

    String id;
    String name;
    Integer version;
    String projectReleaseVersion;

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setProjectReleaseVersion(String projectReleaseVersion) {
        this.projectReleaseVersion = projectReleaseVersion;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public String getProjectReleaseVersion() {
        return projectReleaseVersion;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
