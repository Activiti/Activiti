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
package org.activiti.impl.cmd;

import java.util.List;

import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;

/**
 * @author Joram Barrez
 */
public class DeleteDeploymentCmd extends CmdVoid {

  protected String deploymentId;

  protected boolean cascade;

  public DeleteDeploymentCmd(String deploymentId, boolean cascade) {
    this.deploymentId = deploymentId;
    this.cascade = cascade;
  }

  public void executeVoid(CommandContext commandContext) {
    PersistenceSession persistenceSession = commandContext.getPersistenceSession();
    if (!cascade) {
      persistenceSession.deleteDeployment(deploymentId);
    } else {
      
      List<ProcessDefinitionImpl> procDefs = persistenceSession.findProcessDefinitionsByDeployment(deploymentId);
      for (ProcessDefinitionImpl processDefinition : procDefs) {
        List<DbExecutionImpl> executions = persistenceSession.findRootExecutionsByProcessDefintion(processDefinition.getId());
        for (DbExecutionImpl execution : executions) {
          execution.end();
        }
      }

      // TODO: find better workaround
      persistenceSession.flush(); // need to flush all pending deletes before the process definition can be deleted

      persistenceSession.deleteDeployment(deploymentId);
    }
  }

}
