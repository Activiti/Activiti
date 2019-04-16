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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link AutoDeploymentStrategy} that performs a separate deployment for each resource by name.
 * 

 */
public class SingleResourceAutoDeploymentStrategy extends AbstractAutoDeploymentStrategy {

  /**
   * The deployment mode this strategy handles.
   */
  public static final String DEPLOYMENT_MODE = "single-resource";
  
  private Map<String, String> deployedProcess = new HashMap<>();
  

  @Override
  protected String getDeploymentMode() {
    return DEPLOYMENT_MODE;
  }

  @Override
  public void deployResources(final String deploymentNameHint, final RepositoryService repositoryService) {

    if (processDefinitionResources == null || processDefinitionResources.length <1) {
        return;
    }
      
    // Create a separate deployment for each resource using the resource
    // name

    for (final Resource resource : processDefinitionResources) {

      Entry<String, String> deployEntry = deployProcessFromResource(deploymentNameHint,
                                                                    repositoryService, 
                                                                    resource);
      if (deployEntry != null) {
          deployedProcess.put(deployEntry.getKey(), deployEntry.getValue());
      } 
      
    }
  }

}
