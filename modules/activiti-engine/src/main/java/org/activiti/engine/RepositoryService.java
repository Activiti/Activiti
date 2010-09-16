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

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
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
   * Lists all deployments, ordered by deployment date (ascending).
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
   * Retrieves a list of deployment resources for the given deployment, 
   * ordered alphabetically.
   * @param deploymentId id of the deployment, cannot be null.
   */
  List<String> findDeploymentResourceNames(String deploymentId);
  
  /**
   * Gives access to a deployment resource through a stream of bytes.
   * @param deploymentId id of the deployment, cannot be null.
   * @param resourceName name of the resource, cannot be null.
   * @throws ActivitiException when the resource doesn't exist in the given deployment or when no deployment exists
   * for the given deploymentId.
   */
  InputStream getResourceAsStream(String deploymentId, String resourceName);

  /** [might be impacted by <a href="http://jira.codehaus.org/browse/ACT-66">ACT-66</a>] Get a rendered startform, for collecting parameters from a user to start 
   * a new process instance. Returns null if the processdefinition doesn't have a start form.
   * @param processDefinitionKey process definition key, cannot be null.
   * @throws ActivitiException when no deployed process exists with the given key. 
   */ 
  Object getStartFormByKey(String processDefinitionKey);
  
  /** [might be impacted by <a href="http://jira.codehaus.org/browse/ACT-66">ACT-66</a>] Get a rendered startform, for collecting parameters from a user to start 
   * a new process instance. Returns null if the processdefinition doesn't have a start form. 
   * @param processDefinitionId process definition id, cannot be null.
   * @throws ActivitiException when no deployed process exists with the given key. 
   */ 
  Object getStartFormById(String processDefinitionId);

  
  /** Query process definitions. */
  ProcessDefinitionQuery createProcessDefinitionQuery();
  
  /** Query process definitions. */
  DeploymentQuery createDeploymentQuery();
  
  /**
   * Returns the {@link ProcessDefinition} with the given id, 
   * or null if none is found.
   * @param processDefinitionId id, cannot be null.
   */
  ProcessDefinition findProcessDefinitionById(String processDefinitionId);
  
}
