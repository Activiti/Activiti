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

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerChangeProcessDefinitionSuspensionStateJobHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.impl.persistence.entity.SuspensionState.SuspensionStateUtil;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public abstract class AbstractSetProcessDefinitionStateCmd implements Command<Void> {
  
  protected final String processDefinitionId;
  protected final String processDefinitionKey;
  protected boolean includeProcessInstances = false;
  protected Date executionDate;

  public AbstractSetProcessDefinitionStateCmd(String processDefinitionId, String processDefinitionKey,
            boolean includeProcessInstances, Date executionDate) {
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    this.includeProcessInstances = includeProcessInstances;
    this.executionDate = executionDate;
  }
  
  public Void execute(CommandContext commandContext) {
    
    ProcessDefinitionEntity processDefinitionEntity = findProcessDefinition(commandContext);
    
    if (executionDate != null) { // Process definition state change is delayed
      createTimerForDelayedExecution(commandContext, processDefinitionEntity);
    } else { // Process definition state is changed now
      changeProcessDefinitionState(commandContext, processDefinitionEntity);
    }

    return null;
  }

  protected ProcessDefinitionEntity findProcessDefinition(CommandContext commandContext) {
    
    // Validation of input parameters
    if(processDefinitionId == null && processDefinitionKey == null) {
      throw new ActivitiException("Process definition id or key cannot be null");
    }
    
    ProcessDefinitionEntity processDefinitionEntity = null;
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
    
    if(processDefinitionId == null) {
      processDefinitionEntity = processDefinitionManager.findLatestProcessDefinitionByKey(processDefinitionKey);
      if(processDefinitionEntity == null) {
        throw new ActivitiException("Cannot find process definition for key '"+processDefinitionKey+"'");
      }
    } else {
      processDefinitionEntity = processDefinitionManager.findLatestProcessDefinitionById(processDefinitionId);
      if(processDefinitionEntity == null) {
        throw new ActivitiException("Cannot find process definition for id '"+processDefinitionId+"'");
      }
    }
    return processDefinitionEntity;
  }
  
  protected void createTimerForDelayedExecution(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity) {
    TimerEntity timer = new TimerEntity();
    timer.setDuedate(executionDate);
    timer.setJobHandlerType(getDelayedExecutionJobHandlerType());
    timer.setJobHandlerConfiguration(TimerChangeProcessDefinitionSuspensionStateJobHandler
            .createJobHandlerConfiguration(processDefinitionEntity.getId(), includeProcessInstances));
    commandContext.getJobManager().schedule(timer);
  }
  
  protected void changeProcessDefinitionState(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity) {
    SuspensionStateUtil.setSuspensionState(processDefinitionEntity, getProcessDefinitionSuspensionState());
    
    // Evict cache
    Context
    .getProcessEngineConfiguration()
    .getDeploymentCache().removeProcessDefinition(processDefinitionEntity.getId());
    
    // Suspend process instances (if needed)
    if (includeProcessInstances) {
      
      int currentStartIndex = 0;
      List<ProcessInstance> processInstances = fetchProcessInstancesPage(commandContext, processDefinitionEntity, currentStartIndex);
      while (processInstances.size() > 0) {
        
        for (ProcessInstance processInstance : processInstances) {
          AbstractSetProcessInstanceStateCmd processInstanceCmd = getProcessInstanceChangeStateCmd(processInstance);
          processInstanceCmd.execute(commandContext);
        }
        
        // Fetch new batch of process instances
        currentStartIndex += processInstances.size();
        processInstances = fetchProcessInstancesPage(commandContext, processDefinitionEntity, currentStartIndex);
      }
      
    }
  }
  
  protected List<ProcessInstance> fetchProcessInstancesPage(CommandContext commandContext, 
          ProcessDefinition processDefinition, int currentPageStartIndex) {
    return new ProcessInstanceQueryImpl(commandContext)
      .processDefinitionId(processDefinition.getId())
      .listPage(currentPageStartIndex, Context.getProcessEngineConfiguration().getBatchSizeProcessInstances());
  }
  
  
  // ABSTRACT METHODS ////////////////////////////////////////////////////////////////////

  /**
   * Subclasses should return the wanted {@link SuspensionState} here.
   */
  protected abstract SuspensionState getProcessDefinitionSuspensionState();
  
  /**
   * Subclasses should return the type of the {@link JobHandler} here. it will be used when
   * the user provides an execution date on which the actual state change will happen.
   */
  protected abstract String getDelayedExecutionJobHandlerType();
  
  /**
   * Subclasses should return a {@link Command} implementation that matches the process definition
   * state change.
   */
  protected abstract AbstractSetProcessInstanceStateCmd getProcessInstanceChangeStateCmd(ProcessInstance processInstance); 
  
}
