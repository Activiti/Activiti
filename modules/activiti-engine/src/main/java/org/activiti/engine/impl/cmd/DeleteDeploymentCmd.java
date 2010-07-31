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

import java.util.List;

import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.persistence.RuntimeSession;

/**
 * @author Joram Barrez
 */
public class DeleteDeploymentCmd extends CmdVoid {

  protected String deploymentId;

  public DeleteDeploymentCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public void executeVoid(CommandContext commandContext) {
    RepositorySession repositorySession = commandContext.getRepositorySession();
    RuntimeSession runtimeSession = commandContext.getPersistenceSession();
    
    List<ProcessDefinitionEntity> processDefinitions = repositorySession.findProcessDefinitionsByDeploymentId(deploymentId);
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      List<DbExecutionImpl> executions = runtimeSession.findProcessInstancesByProcessDefintionId(processDefinition.getId());
      for (DbExecutionImpl execution : executions) {
        execution.end();
      }
    }

    repositorySession.deleteDeployment(deploymentId);
  }

}
