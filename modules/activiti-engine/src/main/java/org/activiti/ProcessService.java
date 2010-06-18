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
package org.activiti;

import java.io.InputStream;
import java.util.List;
import java.util.Map;


/** provides access to {@link Deployment}s,
 * {@link ProcessDefinition}s and {@link ProcessInstance}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ProcessService {
  
  /** the process engine from which this process service was obtained */
  ProcessEngine getProcessEngine();

  /** starts creating a new deployment */
  DeploymentBuilder newDeployment();
  
  /** deletes the given deployment and fails if there are still process instances or jobs for this deployment. 
   * @throws ActivitiException if there are still process instances or jobs related to this deployment. */
  void deleteDeployment(String deploymentId);
  
  /** deletes the given deployment and cascade deletion to process instances and jobs */
  void deleteDeploymentCascade(String deploymentId);
  
  /** starts a new process instance in the latest version of the process definition with the given key */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey);

  /** starts a new process instance in the latest version of the process definition with the given key */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables);

  /** starts a new process instance in the exactly specified version of the process definition with the given id */
  ProcessInstance startProcessInstanceById(String processDefinitionId);
  
  /** starts a new process instance in the exactly specified version of the process definition with the given id */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables);
  
  /** delete an existing runtime process instance */
  void deleteProcessInstance(String processInstance);
  
  /** gets the details of a process instance 
   * @return the process instance or null if no process instance could be found with the given id.
   */
  ProcessInstance findProcessInstanceById(String id);

  /** 
   * lists all versions of all process definitions ordered by 
   * key (asc) and version (desc). 
   */
  List<ProcessDefinition> findProcessDefinitions();
  
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
  List<String> findDeploymentResources(String deploymentId);
  
  /**
   * gives access to a deployment resource through a stream of bytes.
   */
  InputStream getDeploymentResourceContent(String deploymentId, String resourceName);
  
  /**
   * creates a new {@link ProcessInstanceQuery} instance, 
   * that can be used to dynamically query the process instances.
   */
  ProcessInstanceQuery createProcessInstanceQuery();
}