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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Joram Barrez
 */
public class SetProcessDefinitionCategoryCmd implements Command<Void> {

  protected String processDefinitionId;
  protected String category;
  
  public SetProcessDefinitionCategoryCmd(String processDefinitionId, String category) {
    this.processDefinitionId = processDefinitionId;
    this.category = category;
  }
  
  public Void execute(CommandContext commandContext) {
    
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Process definition id is null");
    }
    
    ProcessDefinitionEntity processDefinition = commandContext
            .getProcessDefinitionEntityManager()
            .findProcessDefinitionById(processDefinitionId);

    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("No process definition found for id = '" + processDefinitionId + "'", ProcessDefinition.class);
    }
    
    // Update category
    processDefinition.setCategory(category);
    
    // Remove process definition from cache, it will be refetched later
    DeploymentCache<ProcessDefinitionEntity> processDefinitionCache = 
        commandContext.getProcessEngineConfiguration().getProcessDefinitionCache();
    if (processDefinitionCache != null) {
      processDefinitionCache.remove(processDefinitionId);
    }
    
    if (commandContext.getEventDispatcher().isEnabled()) {
      commandContext.getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, processDefinition));
    }
    
    return null;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getCategory() {
    return category;
  }
  
  public void setCategory(String category) {
    this.category = category;
  }
  
}
