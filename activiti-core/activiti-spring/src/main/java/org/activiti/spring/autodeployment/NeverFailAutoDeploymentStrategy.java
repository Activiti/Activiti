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
package org.activiti.spring.autodeployment;

import org.activiti.core.common.spring.project.ApplicationUpgradeContextService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class NeverFailAutoDeploymentStrategy extends AbstractAutoDeploymentStrategy {

    protected static final Logger LOGGER = LoggerFactory.getLogger(NeverFailAutoDeploymentStrategy.class);

    public static final String DEPLOYMENT_MODE = "never-fail";

    public NeverFailAutoDeploymentStrategy(ApplicationUpgradeContextService applicationUpgradeContextService) {
        super(applicationUpgradeContextService);
    }

    @Override
    protected String getDeploymentMode() {
        return DEPLOYMENT_MODE;
    }

    @Override
    public void deployResources(String deploymentNameHint, Resource[] resources, RepositoryService repositoryService) {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().enableDuplicateFiltering()
                .name(deploymentNameHint);

        int validProcessCount = 0;
        for (final Resource resource : resources) {
            final String resourceName = determineResourceName(resource);

            if (validateModel(resource, repositoryService)) {
                validProcessCount++;
                deploymentBuilder.addInputStream(resourceName, resource);
            } else {
                LOGGER.error("The following resource wasn't included in the deployment since it is invalid:\n{}",
                              resourceName);
            }
        }

        deploymentBuilder = loadApplicationUpgradeContext(deploymentBuilder);

        if (validProcessCount != 0) {
            deploymentBuilder.deploy();
        }
    }
}
