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
package org.activiti.dmn.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.activiti.dmn.engine.impl.DecisionTableQueryImpl;
import org.activiti.dmn.engine.impl.interceptor.Command;
import org.activiti.dmn.engine.impl.interceptor.CommandContext;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntity;

/**
 * @author Joram Barrez
 */
public class SetDeploymentTenantIdCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String deploymentId;
  protected String newTenantId;

  public SetDeploymentTenantIdCmd(String deploymentId, String newTenantId) {
    this.deploymentId = deploymentId;
    this.newTenantId = newTenantId;
  }

  public Void execute(CommandContext commandContext) {
    if (deploymentId == null) {
      throw new ActivitiDmnIllegalArgumentException("deploymentId is null");
    }

    // Update all entities

    DmnDeploymentEntity deployment = commandContext.getDeploymentEntityManager().findById(deploymentId);
    if (deployment == null) {
      throw new ActivitiDmnObjectNotFoundException("Could not find deployment with id " + deploymentId);
    }
    
    deployment.setTenantId(newTenantId);

    // Doing process instances, executions and tasks with direct SQL updates
    // (otherwise would not be performant)
    commandContext.getDecisionTableEntityManager().updateDecisionTableTenantIdForDeployment(deploymentId, newTenantId);

    // Doing decision tables in memory, cause we need to clear the decision table cache
    List<DmnDecisionTable> decisionTables = new DecisionTableQueryImpl().deploymentId(deploymentId).list();
    for (DmnDecisionTable decisionTable : decisionTables) {
      commandContext.getDmnEngineConfiguration().getDecisionCache().remove(decisionTable.getId());
    }
    
    commandContext.getDeploymentEntityManager().update(deployment);

    return null;

  }

}
