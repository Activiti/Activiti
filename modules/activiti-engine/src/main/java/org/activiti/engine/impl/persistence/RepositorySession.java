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

package org.activiti.engine.impl.persistence;

import java.util.List;

import org.activiti.engine.DeploymentBuilder;
import org.activiti.engine.impl.persistence.repository.Deployer;
import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.impl.definition.ProcessDefinitionImpl;


/**
 * @author Tom Baeyens
 */
public interface RepositorySession {

  /** inserts the deployment and contained resources in the persistent 
   * storage and runs the deployers on the deployment and those 
   * should {@link #insertProcessDefinition(ProcessDefinitionImpl) insert the process definitions}. */
  void deployNew(DeploymentEntity deployment);
  
  /** deletes the deployment and cascades deletion to the contained resources
   * and process definitions */
  void deleteDeployment(String deploymentId);
  
  /** used when {@link DeploymentBuilder#enableDuplicateFiltering()} is called 
   * while building a deployment. */
  DeploymentEntity findLatestDeploymentByName(String deploymentName);
  
  ResourceEntity findResourceByDeploymentIdAndResourceName(String deploymentId, String resourceName);

  /** must be called by deployers that have parsed process definitions when
   * isNew in {@link Deployer#deploy(DeploymentEntity, boolean)} is true. */
  void insertProcessDefinition(ProcessDefinitionImpl processDefinition);

  ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey);
  List<ProcessDefinitionEntity> findUndeployedProcessDefinitionsByDeploymentId(String deploymentId);

  List<DeploymentEntity> findDeployments();
}
