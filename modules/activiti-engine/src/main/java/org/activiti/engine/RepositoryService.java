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

package org.activiti.engine;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinitionQuery;


/** Service providing access to the repository of process definitions and deployments.
 * 
 * @author Tom Baeyens
 */
public interface RepositoryService {

  /** Starts creating a new deployment */
  DeploymentBuilder createDeployment();
  
  /** Deletes the given deployment.
   * @param deploymentId id of the deployment, cannot be null.
   * @throwns RuntimeException if there are still runtime or history process 
   * instances or jobs. 
   */
  void deleteDeployment(String deploymentId);
  
  /**
   * Deletes the given deployment and cascade deletion to process instances, 
   * history process instances and jobs.
   * @param deploymentId id of the deployment, cannot be null.
   */
  void deleteDeploymentCascade(String deploymentId);
  
  /**
   * Retrieves a list of deployment resources for the given deployment, 
   * ordered alphabetically.
   * @param deploymentId id of the deployment, cannot be null.
   */
  List<String> getDeploymentResourceNames(String deploymentId);
  
  /**
   * Gives access to a deployment resource through a stream of bytes.
   * @param deploymentId id of the deployment, cannot be null.
   * @param resourceName name of the resource, cannot be null.
   * @throws ActivitiException when the resource doesn't exist in the given deployment or when no deployment exists
   * for the given deploymentId.
   */
  InputStream getResourceAsStream(String deploymentId, String resourceName);

  /** Query process definitions. */
  ProcessDefinitionQuery createProcessDefinitionQuery();
  
  /** Query process definitions. */
  DeploymentQuery createDeploymentQuery();
}
