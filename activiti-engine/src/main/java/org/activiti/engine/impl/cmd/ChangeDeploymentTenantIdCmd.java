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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

/**

 */
public class ChangeDeploymentTenantIdCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String deploymentId;
  protected String newTenantId;

  public ChangeDeploymentTenantIdCmd(String deploymentId, String newTenantId) {
    this.deploymentId = deploymentId;
    this.newTenantId = newTenantId;
  }

  public Void execute(CommandContext commandContext) {
    if (deploymentId == null) {
      throw new ActivitiIllegalArgumentException("deploymentId is null");
    }

    // Update all entities

    DeploymentEntity deployment = commandContext.getDeploymentEntityManager().findById(deploymentId);
    if (deployment == null) {
      throw new ActivitiObjectNotFoundException("Could not find deployment with id " + deploymentId, Deployment.class);
    }
    
    String oldTenantId = deployment.getTenantId();
    deployment.setTenantId(newTenantId);

    // Doing process instances, executions and tasks with direct SQL updates
    // (otherwise would not be performant)
    commandContext.getProcessDefinitionEntityManager().updateProcessDefinitionTenantIdForDeployment(deploymentId, newTenantId);
    commandContext.getExecutionEntityManager().updateExecutionTenantIdForDeployment(deploymentId, newTenantId);
    commandContext.getTaskEntityManager().updateTaskTenantIdForDeployment(deploymentId, newTenantId);
    commandContext.getJobEntityManager().updateJobTenantIdForDeployment(deploymentId, newTenantId);
    commandContext.getTimerJobEntityManager().updateJobTenantIdForDeployment(deploymentId, newTenantId);
    commandContext.getSuspendedJobEntityManager().updateJobTenantIdForDeployment(deploymentId, newTenantId);
    commandContext.getDeadLetterJobEntityManager().updateJobTenantIdForDeployment(deploymentId, newTenantId);
    commandContext.getEventSubscriptionEntityManager().updateEventSubscriptionTenantId(oldTenantId, newTenantId);

    // Doing process definitions in memory, cause we need to clear the process definition cache
    List<ProcessDefinition> processDefinitions = new ProcessDefinitionQueryImpl().deploymentId(deploymentId).list();
    for (ProcessDefinition processDefinition : processDefinitions) {
      commandContext.getProcessEngineConfiguration().getProcessDefinitionCache().remove(processDefinition.getId());
    }

    // Clear process definition cache
    commandContext.getProcessEngineConfiguration().getProcessDefinitionCache().clear();

    return null;

  }

}
