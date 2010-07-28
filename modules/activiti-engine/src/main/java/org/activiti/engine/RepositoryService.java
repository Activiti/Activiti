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
  
  /** deletes the given deployment and fails if there are still process instances or jobs for this deployment. 
   * @throws ActivitiException if there are still process instances or jobs related to this deployment. */
  void deleteDeployment(String deploymentId);
  
  /** deletes the given deployment and cascade deletion to process instances and jobs */
  void deleteDeploymentCascade(String deploymentId);
  
  /** get a rendered startform, for collecting parameters from a user to start 
   * a new process instance */ 
  Object getStartFormByKey(String processDefinitionKey);
  
  /** get a rendered startform, for collecting parameters from a user to start 
   * a new process instance */ 
  Object getStartFormById(String processDefinitionId);
  

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
   * lists all deployments by name, ordered by deployment date (ascending).
   * 
   * To retrieve the actual bytes of a deployment resource use the
   * operations <i>findDeploymentResources</i> and <i>getDeploymentResource</i>.
   */
  List<Deployment> findDeploymentsByName(String name);
  
  /**
   * retrieves a list of deployment resources for the given deployment, 
   * ordered alphabetically.
   */
  List<String> findDeploymentResources(String deploymentId);
  
  /**
   * gives access to a deployment resource through a stream of bytes.
   */
  InputStream getDeploymentResourceContent(String deploymentId, String resourceName);
  
  /** 
   * lists all versions of all process definitions ordered by 
   * key (asc) and version (desc). 
   */
  List<ProcessDefinition> findProcessDefinitions();
  
  /**
   * returns the {@link ProcessDefinition} with the given id, 
   * or null if none is found.
   */
  ProcessDefinition findProcessDefinitionById(String processDefinitionId);
}
