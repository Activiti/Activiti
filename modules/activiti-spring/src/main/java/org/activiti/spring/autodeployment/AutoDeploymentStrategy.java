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

import org.activiti.engine.RepositoryService;
import org.springframework.core.io.Resource;

/**
 * Strategy interface for implementations of automatically deploying resources.
 * A strategy may perform any amount of deployments for the {@link Resource}s it
 * is provided with.
 * <p>
 * <p>
 * A strategy is capable of handling deployments corresponding to a certain
 * indicated deployment mode. This applicability is verified using the
 * {@link #handlesMode(String)} method.
 *
 * @author Tiese Barrell
 */
public interface AutoDeploymentStrategy {

    /**
     * Determines whether the strategy handles deployments for the provided
     * deployment mode.
     *
     * @param mode the mode to determine handling for
     * @return true if the strategy handles the mode; false otherwise
     */
    boolean handlesMode(final String mode);

    /**
     * Performs deployment for the provided resources, using the provided name as
     * a hint and the provided {@link RepositoryService} to perform deployment(s).
     *
     * @param deploymentName    the hint for the name of deployment(s) performed
     * @param resources         the resources to be deployed
     * @param repositoryService the repository service to use for deployment(s)
     */
    void deployResources(final String deploymentNameHint, final Resource[] resources, final RepositoryService repositoryService);

}
