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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionManager;

/**
 * 
 * @author Daniel Meyer
 */
public abstract class AbstractSetProcessDefinitionStateCmd implements Command<Void> {
  
  protected final String processDefinitionId;
  private final String processDefinitionKey;

  public AbstractSetProcessDefinitionStateCmd(String processDefinitionId, String processDefinitionKey) {
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
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
    
    return null;
  }

  protected abstract void setState(ProcessDefinitionEntity processDefinitionEntity);

}
