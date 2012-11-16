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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionManager;
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
  protected int batchSize = 25;

  public AbstractSetProcessDefinitionStateCmd(String processDefinitionId, String processDefinitionKey) {
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
  }
  
  public AbstractSetProcessDefinitionStateCmd(String processDefinitionId, String processDefinitionKey,
            boolean includeProcessInstances, int batchSize) {
    this(processDefinitionId, processDefinitionKey);
    this.includeProcessInstances = includeProcessInstances;
    this.batchSize = batchSize;
  }
  
  public Void execute(CommandContext commandContext) {
    if(processDefinitionId == null && processDefinitionKey == null) {
      throw new ActivitiException("Process definition id / key cannot be null");
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
    
    setState(processDefinitionEntity);
    
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
          AbstractSetProcessInstanceStateCmd processInstanceCmd = getProcessInstanceCmd(processInstance);
          processInstanceCmd.execute(commandContext);
        }
        
        // Fetch new batch of process instances
        currentStartIndex += processInstances.size();
        processInstances = fetchProcessInstancesPage(commandContext, processDefinitionEntity, currentStartIndex);
      }
      
    }

    return null;
  }
  
  protected List<ProcessInstance> fetchProcessInstancesPage(CommandContext commandContext, 
          ProcessDefinition processDefinition, int currentPageStartIndex) {
    return new ProcessInstanceQueryImpl(commandContext)
      .processDefinitionId(processDefinition.getId())
      .listPage(currentPageStartIndex, batchSize);
  }

  protected abstract void setState(ProcessDefinitionEntity processDefinitionEntity);
  
  protected abstract AbstractSetProcessInstanceStateCmd getProcessInstanceCmd(ProcessInstance processInstance); 
}
