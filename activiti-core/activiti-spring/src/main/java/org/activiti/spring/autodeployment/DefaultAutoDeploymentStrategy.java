/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.core.io.Resource;

/**
 * Default implementation of {@link AutoDeploymentStrategy} that groups all {@link Resource}s into a single deployment. This implementation is equivalent to the previously used implementation.
 */
public class DefaultAutoDeploymentStrategy extends AbstractAutoDeploymentStrategy {

    /**
     * The deployment mode this strategy handles.
     */
    public static final String DEPLOYMENT_MODE = "default";

    public DefaultAutoDeploymentStrategy(ApplicationUpgradeContextService applicationUpgradeContextService) {
        super(applicationUpgradeContextService);
    }

    @Override
    protected String getDeploymentMode() {
        return DEPLOYMENT_MODE;
    }

    @Override
    public void deployResources(final String deploymentNameHint,
                                final Resource[] resources,
                                final RepositoryService repositoryService) {

        // Create a single deployment for all resources using the name hint as
        // the
        // literal name

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().enableDuplicateFiltering().name(deploymentNameHint);

        for (final Resource resource : resources) {
            final String resourceName = determineResourceName(resource);

            deploymentBuilder.addInputStream(resourceName,
                                             resource);
        }

        loadApplicationUpgradeContext(deploymentBuilder).deploy();
    }
}
