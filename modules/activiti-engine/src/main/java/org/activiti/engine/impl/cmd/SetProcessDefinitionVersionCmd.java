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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;


/**
 * Changes the process definition version of an existing process instance.
 * 
 * Warning:
 * This {@link Command} will NOT perform any migration magic and simply set the
 * process definition version in the database, assuming that the user knows,
 * what he or she is doing.
 * 
 * This is only useful for simple migrations. The new process definition MUST
 * have the exact same activity id to make it still run.
 * 
 * Furthermore, activities referenced by sub-executions and jobs that belong to
 * the process instance MUST exist in the new process definition version.
 * 
 * If the process instance is not currently waiting but actively running, then
 * this would be a case for optimistic locking, meaning either the version
 * update or the "real work" wins, i.e., this is a race condition.
 * 
 * @see http://forums.activiti.org/en/viewtopic.php?t=2918
 * @author Falko Menge
 */
public class SetProcessDefinitionVersionCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final String processInstanceId;
  private final String newProcessDefinitionId;

  public SetProcessDefinitionVersionCmd(String processInstanceId, String newProcessDefinitionId) {
    if (processInstanceId == null || processInstanceId.length() < 1) {
      throw new ActivitiException("The process instance id is mandatory, but '" + processInstanceId + "' has been provided.");
    }
    if (newProcessDefinitionId == null || newProcessDefinitionId.length() < 1) {
      throw new ActivitiException("The process definition id is mandatory, but '" + newProcessDefinitionId + "' has been provided.");
    }
    this.processInstanceId = processInstanceId;
    this.newProcessDefinitionId = newProcessDefinitionId;
  }

  public Void execute(CommandContext commandContext) {
    // check that the new process definition is just another version of the same
    // process definition that the process instance is using
    ExecutionEntity processInstance = commandContext
      .getExecutionManager()
      .findExecutionById(processInstanceId);
    if (processInstance == null) {
      throw new ActivitiException("No process instance found for id = '" + processInstanceId + "'.");
    }
    ProcessDefinitionImpl currentProcessDefinitionImpl = processInstance.getProcessDefinition();

    DeploymentCache deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache();
    ProcessDefinitionEntity currentProcessDefinition;
    if (currentProcessDefinitionImpl instanceof ProcessDefinitionEntity) {
      currentProcessDefinition = (ProcessDefinitionEntity) currentProcessDefinitionImpl;
    } else {
      currentProcessDefinition = deploymentCache.findDeployedProcessDefinitionById(currentProcessDefinitionImpl.getId());
    }
    
    ProcessDefinitionEntity newProcessDefinition = deploymentCache.findDeployedProcessDefinitionById(newProcessDefinitionId);
    if (newProcessDefinition == null) {
      throw new ActivitiException("No process definition found for id = '" + newProcessDefinitionId + "'.");
    }
    
    if (!newProcessDefinition.getKey().equals(currentProcessDefinition.getKey())) {
      throw new ActivitiException(
        "The key of the new process definition " +
        "(key = '" + newProcessDefinition.getKey() + "') " +
        "is not equal to that of the process definition " +
        "(key = '" + currentProcessDefinition.getKey() + "') " +
        "currently used by the process instance " +
        "(id = '" + processInstanceId + "').");
    }
    
    // check that the new process definition version contains the current activity
    if (!newProcessDefinition.contains(processInstance.getActivity())) {
      throw new ActivitiException(
        "The new process definition " +
        "(key = '" + newProcessDefinition.getKey() + "') " +
        "does not contain the current activity " +
        "(id = '" + processInstance.getActivity().getId() + "') " +
        "of the process instance " +
        "(id = '" + processInstanceId + "').");
    }

    // switch the process instance to the new process definition version
    processInstance.setProcessDefinition(newProcessDefinition);
    
    // switch the historic process instance to the new process definition version
    HistoricProcessInstanceManager historicProcessInstanceManager = commandContext.getHistoricProcessInstanceManager();
    if (historicProcessInstanceManager.isHistoryEnabled()) {
      HistoricProcessInstanceEntity historicProcessInstance = historicProcessInstanceManager.findHistoricProcessInstance(processInstanceId);
      historicProcessInstance.setProcessDefinitionId(newProcessDefinitionId);
    }

    return null;
  }

}
