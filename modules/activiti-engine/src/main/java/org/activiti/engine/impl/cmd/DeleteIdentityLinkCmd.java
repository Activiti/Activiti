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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.task.IdentityLinkType;


/**
 * @author Tom Baeyens
 */
public class DeleteIdentityLinkCmd implements Command<Object> {

  protected String taskId;
  
  protected String userId;
  
  protected String groupId;
  
  protected String type;
  
  public DeleteIdentityLinkCmd(String taskId, String userId, String groupId, String type) {
    validateParams(userId, groupId, type, taskId);
    this.taskId = taskId;
    this.userId = userId;
    this.groupId = groupId;
    this.type = type;
  }
  
  protected void validateParams(String userId, String groupId, String type, String taskId) {
    if(taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    
    if (type == null) {
      throw new ActivitiException("type is required when adding a new task identity link");
    }
    
    // Special treatment for assignee, group cannot be used an userId may be null
    if (IdentityLinkType.ASSIGNEE.equals(type)) {
      if (groupId != null) {
        throw new ActivitiException("Incompatible usage: cannot use ASSIGNEE" 
                + " together with a groupId");
      }
    } else {
      if (userId == null && groupId == null) {
        throw new ActivitiException("userId and groupId cannot both be null");
      }
    }
  }
  
  public Void execute(CommandContext commandContext) {
    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);
    
    if (task == null) {
      throw new ActivitiException("Cannot find task with id " + taskId);
    }
    
    if (IdentityLinkType.ASSIGNEE.equals(type)) {
      task.setAssignee(null);
    } else {
      task.deleteIdentityLink(userId, groupId, type);
    }
    
    return null;  
  }
  
}
