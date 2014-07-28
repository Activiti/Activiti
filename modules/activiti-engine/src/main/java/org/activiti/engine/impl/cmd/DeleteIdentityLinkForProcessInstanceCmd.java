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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


/**
 * @author Tijs Rademakers
 */
public class DeleteIdentityLinkForProcessInstanceCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String processInstanceId;
  
  protected String userId;
  
  protected String groupId;
  
  protected String type;
  
  public DeleteIdentityLinkForProcessInstanceCmd(String processInstanceId, String userId, String groupId, String type) {
    validateParams(userId, groupId, processInstanceId, type);
    this.processInstanceId = processInstanceId;
    this.userId = userId;
    this.groupId = groupId;
    this.type = type;
  }
  
  protected void validateParams(String userId, String groupId, String processInstanceId, String type) {
    if(processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("processInstanceId is null");
    }
    
    if (type == null) {
        throw new ActivitiIllegalArgumentException("type is required when deleting a process identity link");
      }
    
    if (userId == null && groupId == null) {
      throw new ActivitiIllegalArgumentException("userId and groupId cannot both be null");
    }
  }
  
  public Void execute(CommandContext commandContext) {
	  ExecutionEntity processInstance = commandContext.getExecutionEntityManager().findExecutionById(processInstanceId);
      
    if (processInstance == null) {
      throw new ActivitiObjectNotFoundException("Cannot find process instance with id " + processInstanceId, ExecutionEntity.class);
    }
    
    processInstance.deleteIdentityLink(userId, groupId, type);
    
    commandContext.getHistoryManager().createProcessInstanceIdentityLinkComment(processInstanceId, userId, groupId, type, false);
    
    return null;  
  }
  
}
