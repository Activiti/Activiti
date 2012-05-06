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
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;


/**
 * @author Tijs Rademakers
 */
public class DeleteIdentityLinkForProcessDefinitionCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;
  
  protected String userId;
  
  protected String groupId;
  
  public DeleteIdentityLinkForProcessDefinitionCmd(String processDefinitionId, String userId, String groupId) {
    validateParams(userId, groupId, processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    this.userId = userId;
    this.groupId = groupId;
  }
  
  protected void validateParams(String userId, String groupId, String processDefinitionId) {
    if(processDefinitionId == null) {
      throw new ActivitiException("processDefinitionId is null");
    }
    
    if (userId == null && groupId == null) {
      throw new ActivitiException("userId and groupId cannot both be null");
    }
  }
  
  public Void execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
        .getCommandContext()
        .getProcessDefinitionManager()
        .findLatestProcessDefinitionById(processDefinitionId);
      
    if (processDefinition == null) {
      throw new ActivitiException("Cannot find process definition with id " + processDefinitionId);
    }
    
    processDefinition.deleteIdentityLink(userId, groupId);
    
    return null;  
  }
  
}
