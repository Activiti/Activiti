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


/** provides access to the repository of process definitions and deployments.
 * 
 * @author Tom Baeyens
 */
public interface RepositoryService {

  /** starts creating a new deployment */
  DeploymentBuilder createDeployment();
  
  /** deletes the given deployment and throws an exception if there are 
   * still runtime or history process instances */
  void deleteDeployment(String deploymentId);
  
  /** deletes the given deployment and cascade deletion to process instances and jobs */
  void deleteDeploymentCascade(String deploymentId);
  
  /** 
   * lists all deployments, ordered by deployment date (ascending).
   * 
   * Note that it is impossible to retrieve the deployment resources
   * through the results of this operation, since that would cause a 
   * huge transfer of (possibly) unneeded bytes over the wire.
   * 
   * To retrieve the actual bytes of a deployment resource use the
   * operations <i>findDeploymentResources</i> and <i>getDeploymentResource</i>.
   */
  List<Deployment> findDeployments();

  /**
   * retrieves a list of deployment resources for the given deployment, 
   * ordered alphabetically.
   */
  List<String> findDeploymentResourceNames(String deploymentId);
  
  /**
   * gives access to a deployment resource through a stream of bytes.
   */
  InputStream getResourceAsStream(String deploymentId, String resourceName);

  /** get a rendered startform, for collecting parameters from a user to start 
   * a new process instance */ 
  Object getStartFormByKey(String processDefinitionKey);
  
  /** get a rendered startform, for collecting parameters from a user to start 
   * a new process instance */ 
  Object getStartFormById(String processDefinitionId);

  
  /** query process definitions. */
  ProcessDefinitionQuery createProcessDefinitionQuery();
  
  /**
   * returns the {@link ProcessDefinition} with the given id, 
   * or null if none is found.
   */
  ProcessDefinition findProcessDefinitionById(String processDefinitionId);
  
}
