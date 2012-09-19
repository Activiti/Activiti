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

import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.DiagramLayout;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.IdentityLink;


/** Service providing access to the repository of process definitions and deployments.
 * 
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Tijs Rademakers
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
   * @deprecated use {@link #deleteDeployment(String, boolean)}.  This methods may be deleted from 5.3. 
   */
  void deleteDeploymentCascade(String deploymentId);

  /**
   * Deletes the given deployment and cascade deletion to process instances, 
   * history process instances and jobs.
   * @param deploymentId id of the deployment, cannot be null.
   */
  void deleteDeployment(String deploymentId, boolean cascade);

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
  
  /**
   * Suspends the process definition with the given id. 
   * 
   * If a process definition is in state suspended, activiti will not 
   * execute jobs (timers, messages) associated with any process instance of the given definition.
   * 
   *  @throws ActivitiException if no such processDefinition can be found or if the process definition is already in state suspended.
   */
  void suspendProcessDefinitionById(String processDefinitionId);
  
  /**
   * Suspends the process definition with the given key (=id in the bpmn20.xml file). 
   * 
   * If a process definition is in state suspended, activiti will not 
   * execute jobs (timers, messages) associated with any process instance of the given definition.
   * 
   * @throws ActivitiException if no such processDefinition can be found or if the process definition is already in state suspended.
   */
  void suspendProcessDefinitionByKey(String processDefinitionKey);
  
  /**
   * Activates the process definition with the given id. 
   * 
   * @throws ActivitiException if no such processDefinition can be found or if the process definition is already in state active.
   */
  void activateProcessDefinitionById(String processDefinitionId);
  
  /**
   * Activates the process definition with the given key (=id in the bpmn20.xml file). 
   * 
   * @throws ActivitiException if no such processDefinition can be found or if the process definition is already in state active.
   */
  void activateProcessDefinitionByKey(String processDefinitionKey);

  /**
   * Gives access to a deployed process model, e.g., a BPMN 2.0 XML file,
   * through a stream of bytes.
   *
   * @param processDefinitionId
   *          id of a {@link ProcessDefinition}, cannot be null.
   * @throws ActivitiException
   *           when the process model doesn't exist.
   */
  InputStream getProcessModel(String processDefinitionId);

  /**
   * Gives access to a deployed process diagram, e.g., a PNG image, through a
   * stream of bytes.
   *
   * @param processDefinitionId
   *          id of a {@link ProcessDefinition}, cannot be null.
   * @return null when the diagram resource name of a {@link ProcessDefinition} is null.
   * @throws ActivitiException
   *           when the process diagram doesn't exist.
   */
  InputStream getProcessDiagram(String processDefinitionId);
  
  /**
   * Returns the {@link ProcessDefinition} including all BPMN information like additional 
   * Properties (e.g. documentation).
   */
  ProcessDefinition getProcessDefinition(String processDefinitionId);  

  /**
   * Provides positions and dimensions of elements in a process diagram as
   * provided by {@link RepositoryService#getProcessDiagram(String)}.
   *
   * This method requires a process model and a diagram image to be deployed.
   * @param processDefinitionId id of a {@link ProcessDefinition}, cannot be null.
   * @return Map with process element ids as keys and positions and dimensions as values.
   * @return null when the input stream of a process diagram is null.
   * @throws ActivitiException when the process model or diagram doesn't exist.
   */
  DiagramLayout getProcessDiagramLayout(String processDefinitionId);
  
  /**
   * Authorizes a candidate user for a process definition.
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @throws ActivitiException when the process definition or user doesn't exist.
   */
  void addCandidateStarterUser(String processDefinitionId, String userId);
  
  /**
   * Authorizes a candidate group for a process definition.
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param groupId id of the group involve, cannot be null.
   * @throws ActivitiException when the process definition or group doesn't exist.
   */
  void addCandidateStarterGroup(String processDefinitionId, String groupId);
  
  /**
   * Removes the authorization of a candidate user for a process definition.
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @throws ActivitiException when the process definition or user doesn't exist.
   */
  void deleteCandidateStarterUser(String processDefinitionId, String userId);
  
  /**
   * Removes the authorization of a candidate group for a process definition.
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param groupId id of the group involve, cannot be null.
   * @throws ActivitiException when the process definition or group doesn't exist.
   */
  void deleteCandidateStarterGroup(String processDefinitionId, String groupId);

  /**
   * Retrieves the {@link IdentityLink}s associated with the given process definition.
   * Such an {@link IdentityLink} informs how a certain identity (eg. group or user)
   * is authorized for a certain process definition
   */
  List<IdentityLink> getIdentityLinksForProcessDefinition(String processDefinitionId);

}
